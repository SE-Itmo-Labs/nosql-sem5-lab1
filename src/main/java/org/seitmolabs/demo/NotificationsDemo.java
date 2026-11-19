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
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;

@Order(60)
@RequiredArgsConstructor
class NotificationsDemo implements DemoScenario {
    private final CategoryDbRepository categories;
    private final MongoNotificationRepository notifications;
    private final NotificationFactory factory;
    private final MongoTemplate mongoTemplate;

    @Override
    public void run() {
        Long categoryId = categories.findBySystemCode(Category.BOOKING_CODE).orElseThrow().getId();
        Instant now = Instant.now();
        List<Example> examples = List.of(
                new Example("confirm-pending", "user", OutboxEventType.ORDER_CONFIRMED, "Билеты готовы",
                        "Бронирование на «Северный свет» подтверждено.", 1, false, null),
                new Example("confirm-processed", "user", OutboxEventType.ORDER_CONFIRMED, "Билеты прочитаны",
                        "Бронирование на «Последний сигнал» подтверждено.", 2, true, ProcessingRuleDemo.CONFIRMED_RULE_ID),
                new Example("cancel-processed", "user", OutboxEventType.ORDER_CANCELLED, "Заказ отменён",
                        "Билеты на «Бумажный дракон» возвращены.", 3, false, ProcessingRuleDemo.CANCELLED_RULE_ID),
                new Example("hold-pending", "user", OutboxEventType.SEAT_HOLD_CREATED, "Место удержано",
                        "Место на «Ночной экспресс» было временно зарезервировано.", 4, false, null),
                new Example("guest-confirm", "demo-guest", OutboxEventType.ORDER_CONFIRMED, "Билеты другого зрителя",
                        "Бронирование второго зрителя подтверждено.", 5, true, ProcessingRuleDemo.CONFIRMED_RULE_ID)
        );
        for (Example example : examples) {
            insertIfMissing(example, categoryId, now.minus(example.hoursAgo(), ChronoUnit.HOURS));
        }
    }

    private void insertIfMissing(Example example, Long categoryId, Instant createdAt) {
        // A stable event ID lets startup reuse the normal duplicate-delivery protection.
        UUID eventId = UUID.nameUUIDFromBytes(("nosql-lab2-demo:" + example.key()).getBytes(StandardCharsets.UTF_8));
        if (notifications.existsBySourceEventId(eventId.toString())) {
            return;
        }
        Notification notification = factory.createSystem(OutboxEvent.builder()
                .id(eventId)
                .eventType(example.eventType())
                .username(example.username())
                .title("[Демо] " + example.title())
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
            actions.add(new NotificationAction(NotificationAction.Type.READ, example.username(),
                    createdAt.plusSeconds(120), null));
        }
        notification.setActionLog(actions);
        notification.setUpdatedAt(actions.getLast().at());
        notifications.saveSystem(notification);
    }

    private record Example(String key, String username, OutboxEventType eventType, String title, String message,
                           int hoursAgo, boolean read, Long ruleId) {
    }
}
