package org.seitmolabs.modules.booking.hold.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seitmolabs.modules.booking.hold.repository.HoldOutboxRepository;
import org.seitmolabs.modules.notification.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HoldOutboxProcessor {

    private String cursor = "0-0";

    private final HoldOutboxRepository outboxRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedDelayString = "${app.notification.outbox-delay:2s}")
    public void deliverPending() {
        var records = outboxRepository.findAfter(cursor);

        if (records.isEmpty()) {
            cursor = "0-0";
            return;
        }

        for (var record : records) {
            cursor = record.getId().getValue();

            try {
                var event = outboxRepository.deserialize(record);

                notificationService.deliver(event);

                outboxRepository.deleteDelivered(record.getId());

            } catch (RuntimeException exception) {
                log.warn(
                        "Не удалось доставить событие удержания {}",
                        record.getId(),
                        exception
                );
            }
        }
    }
}
