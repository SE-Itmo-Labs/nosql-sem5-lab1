package org.seitmolabs.modules.booking.order.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Comparator;

import org.seitmolabs.modules.booking.order.domain.CinemaOrder;
import org.seitmolabs.modules.booking.order.domain.OrderProduct;
import org.seitmolabs.modules.booking.order.domain.Ticket;
import org.seitmolabs.modules.booking.order.dto.OrderProductResponse;
import org.seitmolabs.modules.booking.order.dto.OrderResponse;
import org.seitmolabs.modules.booking.order.dto.TicketResponse;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.screening.repository.ScreeningRepository;
import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.seitmolabs.modules.catalog.seat.repository.SeatRepository;

@Component
@RequiredArgsConstructor
public class OrderResponseMapper {
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;

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
                tickets.stream().map(this::toTicket)
                        .sorted(Comparator.comparingInt(TicketResponse::rowNumber)
                                .thenComparingInt(TicketResponse::seatNumber)).toList(),
                products.stream().map(this::toProduct).toList()
        );
    }

    private TicketResponse toTicket(Ticket ticket) {
        Screening screening = screeningRepository.findById(ticket.getScreeningId())
                .orElseThrow(() -> new EntityNotFoundException("Сеанс не найден: " + ticket.getScreeningId()));
        Seat seat = seatRepository.findById(ticket.getSeatId())
                .orElseThrow(() -> new EntityNotFoundException("Место не найдено: " + ticket.getSeatId()));
        return new TicketResponse(
                ticket.getId(),
                screening.getId(),
                seat.getId(),
                seat.getRowNumber(),
                seat.getSeatNumber(),
                ticket.getPriceSnapshot(),
                ticket.getStatus().name(),
                screening.getMovie().getTitle(),
                screening.getStartsAt(),
                screening.getAuditorium().getName()
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
