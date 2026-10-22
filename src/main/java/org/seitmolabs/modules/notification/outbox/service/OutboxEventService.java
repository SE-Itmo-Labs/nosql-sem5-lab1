package org.seitmolabs.modules.notification.outbox.service;

import java.util.List;

import org.seitmolabs.modules.booking.hold.domain.SeatHold;
import org.seitmolabs.modules.booking.order.domain.CinemaOrder;
import org.seitmolabs.modules.notification.outbox.factory.OutboxEventFactory;
import org.seitmolabs.modules.notification.outbox.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventService {
    private final OutboxEventRepository repository;
    private final OutboxEventFactory factory;

    public void enqueueHoldCreated(SeatHold hold, String username) {
        repository.save(factory.holdCreated(hold, username));
    }

    public void enqueueOrderConfirmed(CinemaOrder order, List<Long> seatIds) {
        repository.save(factory.orderConfirmed(order, seatIds));
    }

    public void enqueueOrderCancelled(CinemaOrder order) {
        repository.save(factory.orderCancelled(order));
    }
}
