package org.seitmolabs.modules.catalog.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.seitmolabs.modules.catalog.dto.SeatAvailabilityStatus;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.springframework.stereotype.Component;

/**
 * Iteration 1 has no orders or Redis holds yet, so every configured seat is
 * available. The resolver keeps that temporary rule out of the catalog service.
 */
@Component
public class AvailableSeatResolver implements SeatAvailabilityResolver {

    @Override
    public Map<Long, SeatAvailabilityStatus> resolve(Screening screening, List<Seat> seats) {
        return seats.stream().collect(Collectors.toMap(
                Seat::getId,
                seat -> SeatAvailabilityStatus.AVAILABLE
        ));
    }
}
