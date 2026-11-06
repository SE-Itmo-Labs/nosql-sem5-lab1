package org.seitmolabs.modules.booking.order.factory;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import org.seitmolabs.modules.booking.hold.domain.SeatHold;
import org.seitmolabs.modules.booking.order.domain.CinemaOrder;
import org.seitmolabs.modules.booking.order.domain.OrderProduct;
import org.seitmolabs.modules.booking.order.domain.OrderStatus;
import org.seitmolabs.modules.booking.order.domain.Ticket;
import org.seitmolabs.modules.booking.order.domain.TicketStatus;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.seitmolabs.modules.product.domain.Product;
import org.seitmolabs.modules.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class OrderFactory {

    private final Clock clock = Clock.systemUTC();

    public CinemaOrder create(
            User user,
            SeatHold hold,
            String idempotencyKey,
            BigDecimal totalAmount
    ) {
        Instant now = Instant.now(clock);
        return CinemaOrder.builder()
                .user(user)
                .holdId(hold.id())
                .idempotencyKey(idempotencyKey)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(totalAmount)
                .createdAt(now)
                .confirmedAt(now)
                .build();
    }

    public Ticket createTicket(CinemaOrder order, Screening screening, Seat seat) {
        return Ticket.builder()
                .order(order)
                .screening(screening)
                .seat(seat)
                .priceSnapshot(screening.getBasePrice())
                .status(TicketStatus.CONFIRMED)
                .build();
    }

    public OrderProduct createProduct(
            CinemaOrder order,
            Product product,
            int quantity
    ) {
        return OrderProduct.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .unitPriceSnapshot(product.getPrice())
                .build();
    }
}
