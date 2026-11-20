package org.seitmolabs.modules.booking.hold.dto;

import java.time.Instant;
import java.util.List;

public record SeatHoldResponse(
        String holdId,
        Long screeningId,
        List<Long> seatIds,
        Instant createdAt,
        Instant expiresAt,
        long remainingSeconds,
        String status
) {
}
