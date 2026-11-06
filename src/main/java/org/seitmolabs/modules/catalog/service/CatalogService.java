package org.seitmolabs.modules.catalog.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.seitmolabs.modules.catalog.dto.MovieResponse;
import org.seitmolabs.modules.catalog.dto.ScreeningResponse;
import org.seitmolabs.modules.catalog.dto.ScreeningSeatsResponse;
import org.seitmolabs.modules.catalog.dto.SeatAvailabilityStatus;
import org.seitmolabs.modules.catalog.mapper.CatalogResponseMapper;
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
    private final CatalogResponseMapper responseMapper;

    public List<MovieResponse> getMovies() {
        return movieRepository.findAllByStatusNotOrderByTitleAsc(MovieStatus.ARCHIVED)
                .stream()
                .map(responseMapper::toMovie)
                .toList();
    }

    public MovieResponse getMovie(Long movieId) {
        return responseMapper.toMovie(findVisibleMovie(movieId));
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
                .map(responseMapper::toScreening)
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

        return responseMapper.toSeatMap(screening, seats, statuses);
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

}
