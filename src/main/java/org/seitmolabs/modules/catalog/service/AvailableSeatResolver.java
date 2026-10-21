package org.seitmolabs.modules.catalog.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.seitmolabs.modules.booking.hold.repository.SeatHoldRepository;
import org.seitmolabs.modules.booking.order.domain.TicketStatus;
import org.seitmolabs.modules.booking.order.repository.TicketRepository;
import org.seitmolabs.modules.catalog.dto.SeatAvailabilityStatus;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AvailableSeatResolver implements SeatAvailabilityResolver {

    private final SeatHoldRepository holdRepository;
    private final TicketRepository ticketRepository;

    @Override
    public Map<Long, SeatAvailabilityStatus> resolve(Screening screening, List<Seat> seats) {
        Set<Long> bookedSeatIds = Set.copyOf(
                ticketRepository.findSeatIds(screening.getId(), TicketStatus.CONFIRMED));

        return seats.stream().collect(Collectors.toMap(
                Seat::getId,
                seat -> resolveStatus(screening.getId(), seat.getId(), bookedSeatIds)
        ));
    }

    private SeatAvailabilityStatus resolveStatus(
            Long screeningId,
            Long seatId,
            Set<Long> bookedSeatIds
    ) {
        if (bookedSeatIds.contains(seatId)) {
            return SeatAvailabilityStatus.BOOKED;
        }
        if (holdRepository.isHeld(screeningId, seatId)) {
            return SeatAvailabilityStatus.HELD;
        }
        return SeatAvailabilityStatus.AVAILABLE;
    }
}
