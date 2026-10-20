package org.seitmolabs.modules.catalog.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.seitmolabs.modules.catalog.dto.AuditoriumResponse;
import org.seitmolabs.modules.catalog.dto.MovieResponse;
import org.seitmolabs.modules.catalog.dto.ScreeningResponse;
import org.seitmolabs.modules.catalog.dto.ScreeningSeatsResponse;
import org.seitmolabs.modules.catalog.dto.SeatAvailabilityStatus;
import org.seitmolabs.modules.catalog.dto.SeatResponse;
import org.seitmolabs.modules.catalog.movie.domain.Movie;
import org.seitmolabs.modules.catalog.movie.domain.MovieStatus;
import org.seitmolabs.modules.catalog.movie.repository.MovieRepository;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.screening.domain.ScreeningStatus;
import org.seitmolabs.modules.catalog.screening.repository.ScreeningRepository;
import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.seitmolabs.modules.catalog.seat.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final SeatAvailabilityResolver seatAvailabilityResolver;

    public List<MovieResponse> getMovies() {
        return movieRepository.findAllByStatusNotOrderByTitleAsc(MovieStatus.ARCHIVED)
                .stream()
                .map(this::toMovieResponse)
                .toList();
    }

    public MovieResponse getMovie(Long movieId) {
        return toMovieResponse(findVisibleMovie(movieId));
    }

    public List<ScreeningResponse> getMovieScreenings(Long movieId) {
        findVisibleMovie(movieId);

        return screeningRepository
                .findAllByMovieIdAndStartsAtAfterAndStatusOrderByStartsAtAsc(
                        movieId,
                        Instant.now(),
                        ScreeningStatus.SCHEDULED
                )
                .stream()
                .map(this::toScreeningResponse)
                .toList();
    }

    public ScreeningSeatsResponse getScreeningSeats(Long screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
                .filter(this::isBookable)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Доступный сеанс не найден: " + screeningId
                ));

        List<Seat> seats = seatRepository
                .findAllByAuditoriumIdOrderByRowNumberAscSeatNumberAsc(
                        screening.getAuditorium().getId()
                );
        Map<Long, SeatAvailabilityStatus> statuses = seatAvailabilityResolver.resolve(screening, seats);

        List<SeatResponse> seatResponses = seats.stream()
                .map(seat -> new SeatResponse(
                        seat.getId(),
                        seat.getRowNumber(),
                        seat.getSeatNumber(),
                        seat.getType(),
                        statuses.getOrDefault(seat.getId(), SeatAvailabilityStatus.AVAILABLE)
                ))
                .toList();

        return new ScreeningSeatsResponse(
                screening.getId(),
                screening.getMovie().getId(),
                screening.getMovie().getTitle(),
                screening.getStartsAt(),
                screening.getBasePrice(),
                new AuditoriumResponse(
                        screening.getAuditorium().getId(),
                        screening.getAuditorium().getName()
                ),
                seatResponses
        );
    }

    private Movie findVisibleMovie(Long movieId) {
        return movieRepository.findById(movieId)
                .filter(movie -> movie.getStatus() != MovieStatus.ARCHIVED)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Фильм не найден: " + movieId
                ));
    }

    private boolean isBookable(Screening screening) {
        return screening.getStatus() == ScreeningStatus.SCHEDULED
                && screening.getStartsAt().isAfter(Instant.now());
    }

    private MovieResponse toMovieResponse(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getSlug(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getDurationMinutes(),
                movie.getAgeRating(),
                movie.getStatus()
        );
    }

    private ScreeningResponse toScreeningResponse(Screening screening) {
        return new ScreeningResponse(
                screening.getId(),
                screening.getMovie().getId(),
                screening.getMovie().getTitle(),
                screening.getAuditorium().getId(),
                screening.getAuditorium().getName(),
                screening.getStartsAt(),
                screening.getBasePrice(),
                screening.getStatus()
        );
    }
}
