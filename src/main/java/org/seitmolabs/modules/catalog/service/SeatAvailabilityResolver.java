package org.seitmolabs.modules.catalog.service;

import java.util.List;
import java.util.Map;

import org.seitmolabs.modules.catalog.dto.SeatAvailabilityStatus;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.seat.domain.Seat;

/**
 * Defines the boundary between the catalog and booking modules. Later iterations
 * will merge permanent tickets from PostgreSQL and active holds from Redis here.
 */
public interface SeatAvailabilityResolver {

    Map<Long, SeatAvailabilityStatus> resolve(Screening screening, List<Seat> seats);
}
