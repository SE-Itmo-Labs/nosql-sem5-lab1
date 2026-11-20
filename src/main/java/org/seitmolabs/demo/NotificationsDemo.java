package org.seitmolabs.demo;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationAction;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.seitmolabs.modules.notification.domain.ProcessingRule;
import org.seitmolabs.modules.notification.factory.NotificationFactory;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEventType;
import org.seitmolabs.modules.notification.repositories.MongoNotificationRepository;
import org.seitmolabs.modules.notification.repositories.NotificationSearchCache;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.seitmolabs.demo.UsersDemo.DEMO_USERNAME;

@Order(60)
@RequiredArgsConstructor
class NotificationsDemo implements DemoScenario {
    private static final Example EXTRA_CONFIRMATION = new Example("guest-confirm", OutboxEventType.ORDER_CONFIRMED,
            "Бронирование подтверждено", "Билеты на «Ночной экспресс» готовы.", 5, true,
            ProcessingRuleDemo.CONFIRMED_RULE_ID);

    private final CategoryDbRepository categories;
    private final MongoNotificationRepository notifications;
    private final NotificationFactory factory;
    private final MongoTemplate mongoTemplate;
    private final NotificationSearchCache searchCache;

    @Override
    public void run() {
        Long categoryId = categories.findBySystemCode(Category.BOOKING_CODE).orElseThrow().getId();
        boolean changed = moveLegacyGuestNotification();
        Instant now = Instant.now();
        List<Example> examples = List.of(
                new Example("confirm-pending", OutboxEventType.ORDER_CONFIRMED, "Билеты готовы",
                        "Бронирование на «Северный свет» подтверждено.", 1, false, null),
                new Example("confirm-processed", OutboxEventType.ORDER_CONFIRMED, "Билеты прочитаны",
                        "Бронирование на «Последний сигнал» подтверждено.", 2, true, ProcessingRuleDemo.CONFIRMED_RULE_ID),
                new Example("cancel-processed", OutboxEventType.ORDER_CANCELLED, "Заказ отменён",
                        "Билеты на «Бумажный дракон» возвращены.", 3, false, ProcessingRuleDemo.CANCELLED_RULE_ID),
                new Example("hold-pending", OutboxEventType.SEAT_HOLD_CREATED, "Место удержано",
                        "Место на «Ночной экспресс» было временно зарезервировано.", 4, false, null),
                EXTRA_CONFIRMATION
        );
        for (Example example : examples) {
            changed |= insertIfMissing(example, categoryId, now.minus(example.hoursAgo(), ChronoUnit.HOURS));
        }
        if (changed) {
            searchCache.invalidate(DEMO_USERNAME);
        }
    }

    private boolean moveLegacyGuestNotification() {
        // Reassign only the original seeded fixture, keeping its identity and read status.
        String formerUsername = "demo-guest";
        Query query = Query.query(Criteria.where("sourceEventId").is(EXTRA_CONFIRMATION.eventId().toString())
                .and("userId").is(formerUsername));
        Update update = new Update()
                .set("userId", DEMO_USERNAME)
                .set("title", EXTRA_CONFIRMATION.displayTitle())
                .set("text", EXTRA_CONFIRMATION.message())
                .set("actionLog.$[read].actorId", DEMO_USERNAME)
                .filterArray(Criteria.where("read.actorId").is(formerUsername)
                        .and("read.type").is(NotificationAction.Type.READ.name()));
        if (mongoTemplate.updateFirst(query, update, Notification.class).getModifiedCount() == 0) {
            return false;
        }
        searchCache.invalidate(formerUsername);
        return true;
    }

    private boolean insertIfMissing(Example example, Long categoryId, Instant createdAt) {
        // A stable event ID lets startup reuse the normal duplicate-delivery protection.
        UUID eventId = example.eventId();
        if (notifications.existsBySourceEventId(eventId.toString())) {
            return false;
        }
        Notification notification = factory.createSystem(OutboxEvent.builder()
                .id(eventId)
                .eventType(example.eventType())
                .username(DEMO_USERNAME)
                .title(example.displayTitle())
                .message(example.message())
                .build(), categoryId);
        notification.setCreatedAt(createdAt);

        // Record example histories as part of the initial insert, leaving subsequent edits intact.
        List<NotificationAction> actions = new ArrayList<>();
        actions.add(new NotificationAction(NotificationAction.Type.CREATED, "system", createdAt, null));
        if (example.ruleId() != null) {
            ProcessingRule rule = mongoTemplate.findById(example.ruleId(), ProcessingRule.class);
            if (rule != null && rule.eventType() == example.eventType() && rule.tagsToAdd() != null) {
                notification.setProcessingRuleId(rule.id());
                notification.setTags(rule.tagsToAdd());
                actions.add(new NotificationAction(NotificationAction.Type.RULE_APPLIED, "admin",
                        createdAt.plusSeconds(60), rule.id()));
            }
        }
        if (example.read()) {
            notification.setStatus(NotificationStatus.READ);
            actions.add(new NotificationAction(NotificationAction.Type.READ, DEMO_USERNAME,
                    createdAt.plusSeconds(120), null));
        }
        notification.setActionLog(actions);
        notification.setUpdatedAt(actions.getLast().at());
        notifications.saveSystem(notification);
        return true;
    }

    private record Example(String key, OutboxEventType eventType, String title, String message,
                           int hoursAgo, boolean read, Long ruleId) {
        private UUID eventId() {
            return UUID.nameUUIDFromBytes(("nosql-lab2-demo:" + key).getBytes(StandardCharsets.UTF_8));
        }

        private String displayTitle() {
            return "[Демо] " + title;
        }
    }
}
