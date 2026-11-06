package org.seitmolabs.modules.catalog.dto;

import org.seitmolabs.modules.catalog.movie.domain.MovieStatus;

public record MovieResponse(
        Long id,
        String slug,
        String title,
        String description,
        int durationMinutes,
        int ageRating,
        MovieStatus status
) {
}
