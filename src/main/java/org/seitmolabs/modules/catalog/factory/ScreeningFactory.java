package org.seitmolabs.modules.catalog.factory;

import java.math.BigDecimal;
import java.time.Instant;

import org.seitmolabs.modules.catalog.auditorium.domain.Auditorium;
import org.seitmolabs.modules.catalog.movie.domain.Movie;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.screening.domain.ScreeningStatus;
import org.springframework.stereotype.Component;

/** Creates screenings while keeping creation rules outside seed and API code. */
@Component
public class ScreeningFactory {

    public Screening createScheduled(
            Movie movie,
            Auditorium auditorium,
            Instant startsAt,
            BigDecimal basePrice
    ) {
        if (movie == null || auditorium == null || startsAt == null || basePrice == null) {
            throw new IllegalArgumentException("Фильм, зал, время и цена сеанса обязательны");
        }
        if (!auditorium.isActive()) {
            throw new IllegalArgumentException("Нельзя создать сеанс в неактивном зале");
        }
        if (basePrice.signum() <= 0) {
            throw new IllegalArgumentException("Цена сеанса должна быть положительной");
        }

        return Screening.builder()
                .movie(movie)
                .auditorium(auditorium)
                .startsAt(startsAt)
                .basePrice(basePrice)
                .status(ScreeningStatus.SCHEDULED)
                .build();
    }
}
