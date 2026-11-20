package org.seitmolabs.demo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.modules.catalog.auditorium.domain.Auditorium;
import org.seitmolabs.modules.catalog.auditorium.repository.AuditoriumRepository;
import org.seitmolabs.modules.catalog.factory.ScreeningFactory;
import org.seitmolabs.modules.catalog.factory.SeatLayoutFactory;
import org.seitmolabs.modules.catalog.movie.domain.Movie;
import org.seitmolabs.modules.catalog.movie.domain.MovieStatus;
import org.seitmolabs.modules.catalog.movie.repository.MovieRepository;
import org.seitmolabs.modules.catalog.screening.repository.ScreeningRepository;
import org.seitmolabs.modules.catalog.seat.repository.SeatRepository;
import org.springframework.core.annotation.Order;

@Order(50)
@RequiredArgsConstructor
class CatalogDemo implements DemoScenario {
    private static final ZoneId CINEMA_ZONE = ZoneId.of("Europe/Moscow");

    private final MovieRepository movieRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final SeatRepository seatRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatLayoutFactory seatLayoutFactory;
    private final ScreeningFactory screeningFactory;

    @Override
    public void run() {
        Movie northernLights = createMovieIfMissing(
                "northern-lights",
                "Северный свет",
                "Камерная драма о путешествии за полярный круг.",
                112,
                12,
                MovieStatus.NOW_SHOWING
        );
        Movie lastSignal = createMovieIfMissing(
                "last-signal",
                "Последний сигнал",
                "Научно-фантастический триллер о сообщении из глубин космоса.",
                128,
                16,
                MovieStatus.NOW_SHOWING
        );
        Movie paperDragon = createMovieIfMissing(
                "paper-dragon",
                "Бумажный дракон",
                "Семейная анимационная история о дружбе и смелости.",
                94,
                6,
                MovieStatus.NOW_SHOWING
        );
        Movie midnightExpress = createMovieIfMissing(
                "midnight-express",
                "Ночной экспресс",
                "Детектив о пропавшем пассажире ночного поезда.",
                104,
                16,
                MovieStatus.NOW_SHOWING
        );

        Auditorium mainHall = createAuditoriumIfMissing("Зал 1");
        Auditorium smallHall = createAuditoriumIfMissing("Зал 2");

        createSeatsIfMissing(mainHall, 5, 8);
        createSeatsIfMissing(smallHall, 4, 6);

        LocalDate tomorrow = LocalDate.now(CINEMA_ZONE).plusDays(1);
        createScreeningIfMissing(northernLights, mainHall,
                atCinemaTime(tomorrow, 12, 0), new BigDecimal("450.00"));
        createScreeningIfMissing(lastSignal, mainHall,
                atCinemaTime(tomorrow, 16, 0), new BigDecimal("550.00"));
        createScreeningIfMissing(paperDragon, smallHall,
                atCinemaTime(tomorrow, 13, 0), new BigDecimal("350.00"));
        createScreeningIfMissing(northernLights, smallHall,
                atCinemaTime(tomorrow.plusDays(1), 18, 0), new BigDecimal("500.00"));
        createScreeningIfMissing(lastSignal, mainHall,
                atCinemaTime(tomorrow.plusDays(1), 20, 0), new BigDecimal("600.00"));
        createScreeningIfMissing(midnightExpress, mainHall,
                atCinemaTime(tomorrow, 19, 30), new BigDecimal("520.00"));
        createScreeningIfMissing(paperDragon, smallHall,
                atCinemaTime(tomorrow.plusDays(1), 11, 15), new BigDecimal("350.00"));
    }

    private Movie createMovieIfMissing(
            String slug,
            String title,
            String description,
            int durationMinutes,
            int ageRating,
            MovieStatus status
    ) {
        return movieRepository.findBySlug(slug).orElseGet(() -> movieRepository.save(Movie.builder()
                .slug(slug)
                .title(title)
                .description(description)
                .durationMinutes(durationMinutes)
                .ageRating(ageRating)
                .status(status)
                .build()));
    }

    private Auditorium createAuditoriumIfMissing(String name) {
        return auditoriumRepository.findByName(name).orElseGet(() -> auditoriumRepository.save(Auditorium.builder()
                .name(name)
                .active(true)
                .build()));
    }

    private void createSeatsIfMissing(
            Auditorium auditorium,
            int rowCount,
            int seatsPerRow
    ) {
        if (seatRepository.existsByAuditoriumId(auditorium.getId())) {
            return;
        }

        seatRepository.saveAll(seatLayoutFactory.create(auditorium, rowCount, seatsPerRow));
    }

    private void createScreeningIfMissing(
            Movie movie,
            Auditorium auditorium,
            Instant startsAt,
            BigDecimal basePrice
    ) {
        if (screeningRepository.existsByAuditoriumIdAndStartsAt(auditorium.getId(), startsAt)) {
            return;
        }

        screeningRepository.save(screeningFactory.createScheduled(movie, auditorium, startsAt, basePrice));
    }

    private Instant atCinemaTime(LocalDate date, int hour, int minute) {
        return date.atTime(hour, minute).atZone(CINEMA_ZONE).toInstant();
    }
}
