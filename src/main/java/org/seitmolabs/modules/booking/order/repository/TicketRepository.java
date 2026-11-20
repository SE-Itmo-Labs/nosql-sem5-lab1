package org.seitmolabs.modules.booking.order.repository;

import java.util.Collection;
import java.util.List;

import org.seitmolabs.modules.booking.order.domain.Ticket;
import org.seitmolabs.modules.booking.order.domain.TicketStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TicketRepository extends MongoRepository<Ticket, Long> {

    boolean existsByScreeningIdAndSeatIdInAndStatus(Long screeningId, Collection<Long> seatIds, TicketStatus status);

    @Query(value = "{ 'screeningId': ?0, 'status': ?1 }", fields = "{ 'seatId': 1, '_id': 0 }")
    List<Ticket> findSeatProjections(Long screeningId, TicketStatus status);

    default List<Long> findSeatIds(Long screeningId, TicketStatus status) {
        return findSeatProjections(screeningId, status).stream().map(Ticket::getSeatId).toList();
    }

    List<Ticket> findAllByOrderId(Long orderId);
}
