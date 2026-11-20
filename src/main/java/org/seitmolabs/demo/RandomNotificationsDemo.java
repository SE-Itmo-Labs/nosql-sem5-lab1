package org.seitmolabs.demo;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import lombok.RequiredArgsConstructor;
import org.bson.BsonType;
import org.bson.Document;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationAction;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.seitmolabs.modules.notification.repositories.NotificationMongoConfiguration;
import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Order(65)
@RequiredArgsConstructor
class RandomNotificationsDemo implements DemoScenario {
    private static final int NOTIFICATIONS_PER_USER = 50_000;
    private static final int BATCH_SIZE = 1_000;
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
        Instant now = Instant.now();
        Pageable pageRequest = PageRequest.of(0, 100, Sort.by("id"));
        do {
            var page = users.findAll(pageRequest);
            page.forEach(user -> fillHistory(user, categoryIds, now));
            pageRequest = page.nextPageable();
        } while (pageRequest.isPaged());
    }

    private void fillHistory(User user, List<Long> categoryIds, Instant now) {
        for (int start = 0; start < NOTIFICATIONS_PER_USER; start += BATCH_SIZE) {
            fillBatch(user, categoryIds, now, start, Math.min(start + BATCH_SIZE, NOTIFICATIONS_PER_USER));
        }
    }

    private void fillBatch(User user, List<Long> categoryIds, Instant now, int start, int end) {
        // The same slots are reused on restart, including after a partially completed run.
        List<String> eventIds = IntStream.range(start, end)
                .mapToObj(slot -> "nosql-demo-random:" + user.getId() + ":" + slot)
                .toList();
        Set<String> existing = Set.copyOf(mongoTemplate.findDistinct(
                Query.query(Criteria.where("sourceEventId").type(BsonType.STRING.getValue()).in(eventIds)),
                "sourceEventId", Notification.class, String.class));
        List<String> missing = eventIds.stream().filter(eventId -> !existing.contains(eventId)).toList();
        if (missing.isEmpty()) {
            return;
        }

        // Share the application sequence, reserving IDs with one write per batch.
        long firstId = notificationIds.reserveIds(missing.size());
        var writes = IntStream.range(0, missing.size())
                .mapToObj(index -> {
                    String eventId = missing.get(index);
                    Notification notification = randomNotification(user, categoryIds, now, eventId);
                    notification.setId(firstId + index);
                    return insertIfMissing(notification);
                })
                .toList();
        mongoTemplate.getCollection(Notification.COLLECTION_NAME)
                .bulkWrite(writes, new BulkWriteOptions().ordered(false));
    }

    private UpdateOneModel<Document> insertIfMissing(Notification notification) {
        var fields = new Document();
        mongoTemplate.getConverter().write(notification, fields);
        // The equality predicate supplies sourceEventId when MongoDB inserts the document.
        fields.remove("sourceEventId");
        return new UpdateOneModel<>(Filters.eq("sourceEventId", notification.getSourceEventId()),
                new Document("$setOnInsert", fields), new UpdateOptions().upsert(true).hintString("sourceEventId"));
    }

    private Notification randomNotification(User user, List<Long> categoryIds, Instant now, String eventId) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Message message = MESSAGES.get(random.nextInt(MESSAGES.size()));
        Long categoryId = categoryIds.get(random.nextInt(categoryIds.size()));
        long ageSeconds = random.nextLong(60, 30 * SECONDS_PER_DAY);
        Instant createdAt = now.minusSeconds(ageSeconds);
        var created = new NotificationAction(NotificationAction.Type.CREATED, "system", createdAt, null);
        boolean read = random.nextBoolean();
        var notification = new Notification(user.getUsername(), "[Демо] " + message.title(), message.text(),
                categoryId, read ? NotificationStatus.READ : NotificationStatus.SENT, createdAt, eventId);

        if (read) {
            Instant readAt = createdAt.plusSeconds(random.nextLong(1, Math.min(ageSeconds, SECONDS_PER_DAY) + 1));
            notification.setActionLog(List.of(created,
                    new NotificationAction(NotificationAction.Type.READ, user.getUsername(), readAt, null)));
            notification.setUpdatedAt(readAt);
        } else {
            notification.setActionLog(List.of(created));
        }
        return notification;
    }

    private record Message(String title, String text) {
    }
}
