package org.seitmolabs.modules.catalog.screening.domain;

import java.math.BigDecimal;
import java.time.Instant;
import org.seitmolabs.modules.catalog.auditorium.domain.Auditorium;
import org.seitmolabs.modules.catalog.movie.domain.Movie;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
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
@Document(collection = "screenings")
public class Screening {
    @Id
    private Long id;

    @DocumentReference
    private Movie movie;
    @DocumentReference
    private Auditorium auditorium;
    private Instant startsAt;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal basePrice;
    private ScreeningStatus status;
}
