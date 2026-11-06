package org.seitmolabs.modules.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ScreeningSeatsResponse(
        Long screeningId,
        Long movieId,
        String movieTitle,
        Instant startsAt,
        BigDecimal basePrice,
        AuditoriumResponse auditorium,
        List<SeatResponse> seats
) {
}
