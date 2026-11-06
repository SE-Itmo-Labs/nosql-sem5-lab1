package org.seitmolabs.modules.catalog.factory;

import java.util.ArrayList;
import java.util.List;

import org.seitmolabs.modules.catalog.auditorium.domain.Auditorium;
import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.seitmolabs.modules.catalog.seat.domain.SeatType;
import org.springframework.stereotype.Component;

/** Builds a complete, internally consistent seat layout for an auditorium. */
@Component
public class SeatLayoutFactory {

    public List<Seat> create(Auditorium auditorium, int rowCount, int seatsPerRow) {
        if (rowCount <= 0 || seatsPerRow <= 0) {
            throw new IllegalArgumentException("Размеры зала должны быть положительными");
        }

        List<Seat> seats = new ArrayList<>(rowCount * seatsPerRow);
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
        return List.copyOf(seats);
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
}
