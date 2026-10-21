package org.seitmolabs.modules.booking.order.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.seitmolabs.modules.booking.order.domain.CinemaOrder;
import org.seitmolabs.modules.booking.order.domain.OrderProduct;
import org.seitmolabs.modules.booking.order.domain.Ticket;
import org.seitmolabs.modules.booking.order.dto.OrderProductResponse;
import org.seitmolabs.modules.booking.order.dto.OrderResponse;
import org.seitmolabs.modules.booking.order.dto.TicketResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderResponseMapper {

    public OrderResponse toResponse(
            CinemaOrder order,
            List<Ticket> tickets,
            List<OrderProduct> products
    ) {
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getConfirmedAt(),
                order.getCancelledAt(),
                tickets.stream().map(this::toTicket).toList(),
                products.stream().map(this::toProduct).toList()
        );
    }

    private TicketResponse toTicket(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getScreening().getId(),
                ticket.getSeat().getId(),
                ticket.getSeat().getRowNumber(),
                ticket.getSeat().getSeatNumber(),
                ticket.getPriceSnapshot(),
                ticket.getStatus().name()
        );
    }

    private OrderProductResponse toProduct(OrderProduct item) {
        return new OrderProductResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPriceSnapshot(),
                item.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()))
        );
    }
}
