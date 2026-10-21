package org.seitmolabs.modules.notification.outbox.factory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.seitmolabs.modules.booking.hold.domain.SeatHold;
import org.seitmolabs.modules.booking.order.domain.CinemaOrder;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEventType;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventFactory {
    private static final String BOOKING_CATEGORY = "Бронирование";

    public OutboxEvent holdCreated(SeatHold hold, String username) {
        return create(OutboxEventType.SEAT_HOLD_CREATED, hold.id(), username,
                "Места временно удерживаются",
                "Сеанс №%d, места: %s. Удержание действует до %s."
                        .formatted(hold.screeningId(), join(hold.seatIds()), hold.expiresAt()));
    }

    public OutboxEvent orderConfirmed(CinemaOrder order, List<Long> seatIds) {
        return create(OutboxEventType.ORDER_CONFIRMED, order.getId().toString(),
                order.getUser().getUsername(), "Заказ подтверждён",
                "Заказ №%d подтверждён. Места: %s. Сумма: %s."
                        .formatted(order.getId(), join(seatIds), order.getTotalAmount()));
    }

    public OutboxEvent orderCancelled(CinemaOrder order) {
        return create(OutboxEventType.ORDER_CANCELLED, order.getId().toString(),
                order.getUser().getUsername(), "Заказ отменён",
                "Заказ №%d отменён. Билеты снова доступны для покупки.".formatted(order.getId()));
    }

    private OutboxEvent create(OutboxEventType type, String aggregateId, String username,
            String title, String message) {
        return OutboxEvent.builder().id(UUID.randomUUID()).eventType(type).aggregateId(aggregateId)
                .username(username).title(title).message(message).categoryName(BOOKING_CATEGORY)
                .createdAt(Instant.now()).attempts(0).build();
    }

    private String join(List<Long> ids) {
        return ids.stream().map(String::valueOf).reduce((left, right) -> left + ", " + right).orElse("—");
    }
}
