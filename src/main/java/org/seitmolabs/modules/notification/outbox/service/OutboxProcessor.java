package org.seitmolabs.modules.notification.outbox.service;

import java.time.Instant;

import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.seitmolabs.modules.notification.outbox.repository.OutboxEventRepository;
import org.seitmolabs.modules.notification.service.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OutboxProcessor {
    private final OutboxEventRepository repository;
    private final NotificationService notificationService;

    /** A failed delivery remains pending and is retried during the next cycle. */
    @Scheduled(fixedDelayString = "${app.notification.outbox-delay:2s}")
    @Transactional
    public void deliverPending() {
        for (OutboxEvent event : repository.findByDeliveredAtIsNullOrderByCreatedAt(PageRequest.of(0, 20))) {
            try {
                notificationService.deliver(event);
                event.setDeliveredAt(Instant.now());
                event.setLastError(null);
            } catch (RuntimeException exception) {
                event.setAttempts(event.getAttempts() + 1);
                event.setLastError(limit(exception.getMessage()));
            }
        }
    }

    private String limit(String message) {
        String value = message == null ? "Неизвестная ошибка доставки" : message;
        return value.substring(0, Math.min(value.length(), 1000));
    }
}
