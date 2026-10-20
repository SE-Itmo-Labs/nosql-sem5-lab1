package org.seitmolabs.modules.notification.factory;

import java.time.Clock;
import java.time.Instant;

import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
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

    public Notification createSent(Long id, CreateNotificationRequest request) {
        return new Notification(
                id,
                request.userId(),
                request.title(),
                request.text(),
                request.categoryId(),
                NotificationStatus.SENT,
                Instant.now(clock)
        );
    }
}
