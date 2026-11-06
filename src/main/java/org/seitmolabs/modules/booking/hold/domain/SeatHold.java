package org.seitmolabs.modules.booking.hold.domain;

import java.time.Instant;
import java.util.List;

public record SeatHold(
        String id,
        Long userId,
        Long screeningId,
        List<Long> seatIds,
        Instant createdAt,
        Instant expiresAt
) {
}
