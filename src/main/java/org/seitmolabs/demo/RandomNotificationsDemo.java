package org.seitmolabs.demo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import lombok.RequiredArgsConstructor;
import org.bson.BsonType;
import org.bson.Document;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationAction;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.seitmolabs.modules.notification.domain.ProcessingRule;
import org.seitmolabs.modules.notification.repositories.NotificationMongoConfiguration;
import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Order(65)
@RequiredArgsConstructor
class RandomNotificationsDemo implements DemoScenario {
    private static final int NOTIFICATIONS_PER_USER = 1_000;
    private static final long SECONDS_PER_DAY = 86_400L;
    private static final List<Message> MESSAGES = List.of(
            new Message("Обновление афиши", "В расписании появились новые сеансы."),
            new Message("Напоминание о сеансе", "Проверьте время начала и номер зала в личном кабинете."),
            new Message("Премьера недели", "В афише доступен новый фильм."),
            new Message("Специальное предложение", "В кинотеатре действует предложение на вечерние сеансы."),
            new Message("Места снова доступны", "На один из ближайших показов появились свободные места."),
            new Message("Изменение расписания", "Актуальное время сеансов доступно в афише."));

    private final UserRepository users;
    private final CategoryDbRepository categories;
    private final NotificationMongoConfiguration notificationIds;
    private final MongoTemplate mongoTemplate;

    @Override
    public void run() {
        List<Long> categoryIds = categories.findAll().stream().map(Category::getId).toList();
        List<ProcessingRule> rules = List.of(ProcessingRuleDemo.CONFIRMED_RULE_ID,
                        ProcessingRuleDemo.CANCELLED_RULE_ID, ProcessingRuleDemo.HELD_RULE_ID).stream()
                .map(id -> mongoTemplate.findById(id, ProcessingRule.class))
                .filter(Objects::nonNull)
                .filter(rule -> rule.eventType() != null && rule.tagsToAdd() != null)
                .toList();
        Instant now = Instant.now();
        users.findAll().forEach(user -> fillHistory(user, categoryIds, rules, now));
    }

    private void fillHistory(User user, List<Long> categoryIds, List<ProcessingRule> rules, Instant now) {
        // The same slots are reused on restart, including after a partially completed run.
        List<Slot> slots = IntStream.range(0, NOTIFICATIONS_PER_USER)
                .mapToObj(slot -> new Slot("nosql-demo-random:" + user.getId() + ":" + slot,
                        !rules.isEmpty() && slot % 25 == 0 ? rules.get(slot / 25 % rules.size()) : null))
                .toList();
        Set<String> existing = Set.copyOf(mongoTemplate.findDistinct(
                Query.query(Criteria.where("sourceEventId").type(BsonType.STRING.getValue())
                        .in(slots.stream().map(Slot::eventId).toList())),
                "sourceEventId", Notification.class, String.class));
        var writes = new ArrayList<WriteModel<Document>>();
        for (ProcessingRule rule : rules) {
            List<String> previous = slots.stream()
                    .filter(slot -> rule.equals(slot.rule()) && existing.contains(slot.eventId()))
                    .map(Slot::eventId)
                    .toList();
            if (!previous.isEmpty()) {
                writes.add(addRuleToExisting(previous, rule, now));
            }
        }

        List<Slot> missing = slots.stream().filter(slot -> !existing.contains(slot.eventId())).toList();
        if (!missing.isEmpty()) {
            // Share the application sequence, reserving IDs with one write per batch.
            long nextId = notificationIds.reserveIds(missing.size());
            for (Slot slot : missing) {
                Notification notification = randomNotification(user, categoryIds, now, slot);
                notification.setId(nextId++);
                writes.add(insertIfMissing(notification));
            }
        }
        if (!writes.isEmpty()) {
            mongoTemplate.getCollection(Notification.COLLECTION_NAME)
                    .bulkWrite(writes, new BulkWriteOptions().ordered(false));
        }
    }

    private UpdateManyModel<Document> addRuleToExisting(List<String> eventIds, ProcessingRule rule, Instant now) {
        // Upgrade historical fixtures once without changing their owner, ID, or read status.
        var action = new Document("type", NotificationAction.Type.RULE_APPLIED.name())
                .append("actorId", "admin").append("at", Date.from(now)).append("processingRuleId", rule.id());
        var update = new Document("$set", new Document("processingRuleId", rule.id())
                .append("sourceEventType", rule.eventType().name()))
                .append("$addToSet", new Document("tags", new Document("$each", rule.tagsToAdd())))
                .append("$push", new Document("actionLog", action))
                .append("$max", new Document("updatedAt", Date.from(now)));
        return new UpdateManyModel<>(Filters.and(Filters.in("sourceEventId", eventIds),
                Filters.eq("processingRuleId", null)), update, new UpdateOptions().hintString("sourceEventId"));
    }

    private UpdateOneModel<Document> insertIfMissing(Notification notification) {
        var fields = new Document();
        mongoTemplate.getConverter().write(notification, fields);
        // The equality predicate supplies sourceEventId when MongoDB inserts the document.
        fields.remove("sourceEventId");
        return new UpdateOneModel<>(Filters.eq("sourceEventId", notification.getSourceEventId()),
                new Document("$setOnInsert", fields), new UpdateOptions().upsert(true).hintString("sourceEventId"));
    }

    private Notification randomNotification(User user, List<Long> categoryIds, Instant now, Slot slot) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Message message = MESSAGES.get(random.nextInt(MESSAGES.size()));
        Long categoryId = categoryIds.get(random.nextInt(categoryIds.size()));
        long ageSeconds = random.nextLong(60, 30 * SECONDS_PER_DAY);
        Instant createdAt = now.minusSeconds(ageSeconds);
        var created = new NotificationAction(NotificationAction.Type.CREATED, "system", createdAt, null);
        boolean read = random.nextBoolean();
        var notification = new Notification(user.getUsername(), "[Демо] " + message.title(), message.text(),
                categoryId, read ? NotificationStatus.READ : NotificationStatus.SENT, createdAt, slot.eventId());

        var actions = new ArrayList<NotificationAction>();
        actions.add(created);
        if (slot.rule() != null) {
            ProcessingRule rule = slot.rule();
            notification.setSourceEventType(rule.eventType());
            notification.setProcessingRuleId(rule.id());
            notification.setTags(rule.tagsToAdd().stream().distinct().toList());
            actions.add(new NotificationAction(NotificationAction.Type.RULE_APPLIED, "admin", createdAt, rule.id()));
        }
        if (read) {
            Instant readAt = createdAt.plusSeconds(random.nextLong(1, Math.min(ageSeconds, SECONDS_PER_DAY) + 1));
            actions.add(new NotificationAction(NotificationAction.Type.READ, user.getUsername(), readAt, null));
            notification.setUpdatedAt(readAt);
        }
        notification.setActionLog(actions);
        return notification;
    }

    private record Slot(String eventId, ProcessingRule rule) {
    }

    private record Message(String title, String text) {
    }
}
