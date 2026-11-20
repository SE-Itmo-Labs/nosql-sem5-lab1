package org.seitmolabs.modules.notification.outbox.service;

import java.time.Instant;

import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.seitmolabs.modules.notification.outbox.repository.OutboxEventRepository;
import org.seitmolabs.modules.notification.service.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OutboxProcessor {
    private final OutboxEventRepository repository;
    private final NotificationService notificationService;

    /** A failed delivery remains pending and is retried during the next cycle. */
    @Scheduled(fixedDelayString = "${app.notification.outbox-delay:2s}")
    public void deliverPending() {
        for (OutboxEvent event : repository.findByDeliveredAtIsNullOrderByLastAttemptAtAscCreatedAtAsc(PageRequest.of(0, 20))) {
            try {
                notificationService.deliver(event);
                repository.markDelivered(event.getId(), Instant.now());
            } catch (RuntimeException exception) {
                repository.recordFailure(event.getId(), limit(exception.getMessage()), Instant.now());
            }
        }
    }

    private String limit(String message) {
        String value = message == null ? "Неизвестная ошибка доставки" : message;
        return value.substring(0, Math.min(value.length(), 1000));
    }
}
