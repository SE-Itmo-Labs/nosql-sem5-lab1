package org.seitmolabs.modules.catalog.seat.domain;

import org.seitmolabs.modules.catalog.auditorium.domain.Auditorium;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "seats")
public class Seat {
    @Id
    private Long id;

    @DocumentReference
    private Auditorium auditorium;
    private int rowNumber;
    private int seatNumber;
    private SeatType type;
}
