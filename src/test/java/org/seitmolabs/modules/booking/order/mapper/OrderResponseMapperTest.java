package org.seitmolabs.modules.booking.order.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.seitmolabs.modules.booking.order.domain.CinemaOrder;
import org.seitmolabs.modules.booking.order.domain.OrderStatus;
import org.seitmolabs.modules.booking.order.domain.Ticket;
import org.seitmolabs.modules.booking.order.domain.TicketStatus;
import org.seitmolabs.modules.catalog.auditorium.domain.Auditorium;
import org.seitmolabs.modules.catalog.movie.domain.Movie;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.seat.domain.Seat;

class OrderResponseMapperTest {

    @Test
    void ticketContainsScreeningDetailsRatherThanOnlyPurchaseTime() {
        Instant startsAt = Instant.parse("2026-10-22T12:00:00Z");
        Screening screening = Screening.builder().id(11L)
                .movie(Movie.builder().title("Северный свет").build())
                .auditorium(Auditorium.builder().name("Зал 2").build())
                .startsAt(startsAt).build();
        Ticket ticket = Ticket.builder().id(1L).screening(screening)
                .seat(Seat.builder().id(8L).rowNumber(2).seatNumber(4).build())
                .priceSnapshot(new BigDecimal("450.00")).status(TicketStatus.CONFIRMED).build();
        CinemaOrder order = CinemaOrder.builder().id(3L).status(OrderStatus.CONFIRMED)
                .confirmedAt(Instant.parse("2026-10-21T10:00:00Z")).build();

        var response = new OrderResponseMapper().toResponse(order, List.of(ticket), List.of());
        var result = response.tickets().getFirst();

        assertThat(result.movieTitle()).isEqualTo("Северный свет");
        assertThat(result.startsAt()).isEqualTo(startsAt);
        assertThat(result.auditoriumName()).isEqualTo("Зал 2");
        assertThat(result.rowNumber()).isEqualTo(2);
        assertThat(result.seatNumber()).isEqualTo(4);
        assertThat(result.price()).isEqualByComparingTo("450.00");
    }
}
