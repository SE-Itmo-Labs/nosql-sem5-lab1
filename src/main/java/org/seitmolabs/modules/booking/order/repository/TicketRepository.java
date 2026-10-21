package org.seitmolabs.modules.booking.order.repository;

import java.util.Collection;
import java.util.List;

import org.seitmolabs.modules.booking.order.domain.Ticket;
import org.seitmolabs.modules.booking.order.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("""
            select (count(ticket) > 0) from Ticket ticket
            where ticket.screening.id = :screeningId
              and ticket.seat.id in :seatIds
              and ticket.status = :status
            """)
    boolean existsConfirmedByScreeningIdAndSeatIdIn(
            @Param("screeningId") Long screeningId,
            @Param("seatIds") Collection<Long> seatIds,
            @Param("status") TicketStatus status
    );

    @Query("""
            select ticket.seat.id from Ticket ticket
            where ticket.screening.id = :screeningId
              and ticket.status = :status
            """)
    List<Long> findSeatIds(
            @Param("screeningId") Long screeningId,
            @Param("status") TicketStatus status
    );

    @Query("""
            select ticket from Ticket ticket
            join fetch ticket.screening
            join fetch ticket.seat seat
            where ticket.order.id = :orderId
            order by seat.rowNumber, seat.seatNumber
            """)
    List<Ticket> findAllForOrder(@Param("orderId") Long orderId);
}
