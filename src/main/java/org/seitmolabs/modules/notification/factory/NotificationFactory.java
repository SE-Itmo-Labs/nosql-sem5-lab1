package org.seitmolabs.modules.notification.factory;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationAction;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.springframework.stereotype.Component;

/** Creates notifications with a single policy for status and timestamps. */
@Component
public class NotificationFactory {

    private final Clock clock;

    public NotificationFactory() {
        this(Clock.systemUTC());
    }

    NotificationFactory(Clock clock) {
        this.clock = clock;
    }

    public Notification createSent(CreateNotificationRequest request, String actorId) {
        Notification notification = new Notification(
                request.userId(),
                request.title(),
                request.text(),
                request.categoryId(),
                NotificationStatus.SENT,
                Instant.now(clock)
        );
        recordCreation(notification, actorId);
        return notification;
    }

    public Notification createSystem(OutboxEvent event, Long categoryId) {
        Notification notification = new Notification(event.getUsername(), event.getTitle(), event.getMessage(),
                categoryId, NotificationStatus.SENT, Instant.now(clock), event.getId().toString());
        notification.setSourceEventType(event.getEventType());
        recordCreation(notification, "system");
        return notification;
    }

    private void recordCreation(Notification notification, String actorId) {
        notification.setActionLog(List.of(new NotificationAction(
                NotificationAction.Type.CREATED, actorId, notification.getCreatedAt(), null)));
    }
}
