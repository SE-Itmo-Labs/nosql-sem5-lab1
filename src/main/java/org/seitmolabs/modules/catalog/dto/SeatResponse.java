package org.seitmolabs.modules.catalog.dto;

import org.seitmolabs.modules.catalog.seat.domain.SeatType;

public record SeatResponse(
        Long id,
        int rowNumber,
        int seatNumber,
        SeatType type,
        SeatAvailabilityStatus status
) {
}
