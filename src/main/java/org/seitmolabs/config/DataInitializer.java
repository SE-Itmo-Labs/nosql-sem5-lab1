package org.seitmolabs.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.seitmolabs.modules.catalog.auditorium.domain.Auditorium;
import org.seitmolabs.modules.catalog.auditorium.repository.AuditoriumRepository;
import org.seitmolabs.modules.catalog.movie.domain.Movie;
import org.seitmolabs.modules.catalog.movie.domain.MovieStatus;
import org.seitmolabs.modules.catalog.movie.repository.MovieRepository;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.screening.domain.ScreeningStatus;
import org.seitmolabs.modules.catalog.screening.repository.ScreeningRepository;
import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.seitmolabs.modules.catalog.seat.domain.SeatType;
import org.seitmolabs.modules.catalog.seat.repository.SeatRepository;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.enums.Role;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private static final ZoneId CINEMA_ZONE = ZoneId.of("Europe/Moscow");

    @Bean
    public CommandLineRunner createTestData(
            UserRepository userRepository,
            CategoryDbRepository categoryRepository,
            MovieRepository movieRepository,
            AuditoriumRepository auditoriumRepository,
            SeatRepository seatRepository,
            ScreeningRepository screeningRepository
    ) {
        return args -> {
            createUserIfMissing(userRepository, "client01", "client123", "Тестовый клиент", Role.ROLE_USER);
            createUserIfMissing(userRepository, "admin", "123", "Администратор", Role.ROLE_ADMIN);

            createCategoryIfMissing(categoryRepository, "Сеансы", "Изменения времени и зала");
            createCategoryIfMissing(categoryRepository, "Бронирование", "Изменения состояния билета");
            createCategoryIfMissing(categoryRepository, "Акции", "Скидки и специальные предложения");

            Movie northernLights = createMovieIfMissing(
                    movieRepository,
                    "northern-lights",
                    "Северный свет",
                    "Камерная драма о путешествии за полярный круг.",
                    112,
                    12,
                    MovieStatus.NOW_SHOWING
            );
            Movie lastSignal = createMovieIfMissing(
                    movieRepository,
                    "last-signal",
                    "Последний сигнал",
                    "Научно-фантастический триллер о сообщении из глубин космоса.",
                    128,
                    16,
                    MovieStatus.NOW_SHOWING
            );
            Movie paperDragon = createMovieIfMissing(
                    movieRepository,
                    "paper-dragon",
                    "Бумажный дракон",
                    "Семейная анимационная история о дружбе и смелости.",
                    94,
                    6,
                    MovieStatus.NOW_SHOWING
            );

            Auditorium mainHall = createAuditoriumIfMissing(auditoriumRepository, "Зал 1");
            Auditorium smallHall = createAuditoriumIfMissing(auditoriumRepository, "Зал 2");

            createSeatsIfMissing(seatRepository, mainHall, 5, 8);
            createSeatsIfMissing(seatRepository, smallHall, 4, 6);

            LocalDate tomorrow = LocalDate.now(CINEMA_ZONE).plusDays(1);
            createScreeningIfMissing(screeningRepository, northernLights, mainHall,
                    atCinemaTime(tomorrow, 12, 0), new BigDecimal("450.00"));
            createScreeningIfMissing(screeningRepository, lastSignal, mainHall,
                    atCinemaTime(tomorrow, 16, 0), new BigDecimal("550.00"));
            createScreeningIfMissing(screeningRepository, paperDragon, smallHall,
                    atCinemaTime(tomorrow, 13, 0), new BigDecimal("350.00"));
            createScreeningIfMissing(screeningRepository, northernLights, smallHall,
                    atCinemaTime(tomorrow.plusDays(1), 18, 0), new BigDecimal("500.00"));
            createScreeningIfMissing(screeningRepository, lastSignal, mainHall,
                    atCinemaTime(tomorrow.plusDays(1), 20, 0), new BigDecimal("600.00"));
        };
    }

    private void createUserIfMissing(
            UserRepository userRepository,
            String username,
            String password,
            String displayName,
            Role role
    ) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        userRepository.save(User.builder()
                .username(username)
                .password(password)
                .displayName(displayName)
                .role(role)
                .build());
    }

    private void createCategoryIfMissing(
            CategoryDbRepository repository,
            String name,
            String description
    ) {
        if (repository.existsByName(name)) {
            return;
        }

        repository.save(new Category(null, name, description));
    }

    private Movie createMovieIfMissing(
            MovieRepository repository,
            String slug,
            String title,
            String description,
            int durationMinutes,
            int ageRating,
            MovieStatus status
    ) {
        return repository.findBySlug(slug).orElseGet(() -> repository.save(Movie.builder()
                .slug(slug)
                .title(title)
                .description(description)
                .durationMinutes(durationMinutes)
                .ageRating(ageRating)
                .status(status)
                .build()));
    }

    private Auditorium createAuditoriumIfMissing(
            AuditoriumRepository repository,
            String name
    ) {
        return repository.findByName(name).orElseGet(() -> repository.save(Auditorium.builder()
                .name(name)
                .active(true)
                .build()));
    }

    private void createSeatsIfMissing(
            SeatRepository repository,
            Auditorium auditorium,
            int rowCount,
            int seatsPerRow
    ) {
        if (repository.existsByAuditoriumId(auditorium.getId())) {
            return;
        }

        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= rowCount; row++) {
            for (int number = 1; number <= seatsPerRow; number++) {
                seats.add(Seat.builder()
                        .auditorium(auditorium)
                        .rowNumber(row)
                        .seatNumber(number)
                        .type(resolveSeatType(row, rowCount, number))
                        .build());
            }
        }
        repository.saveAll(seats);
    }

    private SeatType resolveSeatType(int row, int rowCount, int seatNumber) {
        if (row == 1 && seatNumber <= 2) {
            return SeatType.ACCESSIBLE;
        }
        if (row == rowCount) {
            return SeatType.PREMIUM;
        }
        return SeatType.STANDARD;
    }

    private void createScreeningIfMissing(
            ScreeningRepository repository,
            Movie movie,
            Auditorium auditorium,
            Instant startsAt,
            BigDecimal basePrice
    ) {
        if (repository.existsByAuditoriumIdAndStartsAt(auditorium.getId(), startsAt)) {
            return;
        }

        repository.save(Screening.builder()
                .movie(movie)
                .auditorium(auditorium)
                .startsAt(startsAt)
                .basePrice(basePrice)
                .status(ScreeningStatus.SCHEDULED)
                .build());
    }

    private Instant atCinemaTime(LocalDate date, int hour, int minute) {
        return date.atTime(hour, minute).atZone(CINEMA_ZONE).toInstant();
    }
}
