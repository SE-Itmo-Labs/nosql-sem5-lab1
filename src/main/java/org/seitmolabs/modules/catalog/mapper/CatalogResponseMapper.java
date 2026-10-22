package org.seitmolabs.modules.catalog.mapper;

import java.util.List;
import java.util.Map;

import org.seitmolabs.modules.catalog.dto.AuditoriumResponse;
import org.seitmolabs.modules.catalog.dto.MovieResponse;
import org.seitmolabs.modules.catalog.dto.ScreeningResponse;
import org.seitmolabs.modules.catalog.dto.ScreeningSeatsResponse;
import org.seitmolabs.modules.catalog.dto.SeatAvailabilityStatus;
import org.seitmolabs.modules.catalog.dto.SeatResponse;
import org.seitmolabs.modules.catalog.movie.domain.Movie;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.springframework.stereotype.Component;

/** Converts catalog entities to stable REST representations. */
@Component
public class CatalogResponseMapper {

    public MovieResponse toMovie(Movie movie) {
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

    public ScreeningResponse toScreening(Screening screening) {
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

    public ScreeningSeatsResponse toSeatMap(
            Screening screening,
            List<Seat> seats,
            Map<Long, SeatAvailabilityStatus> statuses
    ) {
        List<SeatResponse> seatResponses = seats.stream()
                .map(seat -> toSeat(seat, statuses))
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

    private SeatResponse toSeat(
            Seat seat,
            Map<Long, SeatAvailabilityStatus> statuses
    ) {
        return new SeatResponse(
                seat.getId(),
                seat.getRowNumber(),
                seat.getSeatNumber(),
                seat.getType(),
                statuses.getOrDefault(seat.getId(), SeatAvailabilityStatus.AVAILABLE)
        );
    }
}
