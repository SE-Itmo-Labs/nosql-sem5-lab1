package org.seitmolabs.modules.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;

import org.seitmolabs.modules.catalog.screening.domain.ScreeningStatus;

public record ScreeningResponse(
        Long id,
        Long movieId,
        String movieTitle,
        Long auditoriumId,
        String auditoriumName,
        Instant startsAt,
        BigDecimal basePrice,
        ScreeningStatus status
) {
}
