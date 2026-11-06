package org.seitmolabs.modules.catalog.screening.domain;

import java.math.BigDecimal;
import java.time.Instant;

import org.seitmolabs.modules.catalog.auditorium.domain.Auditorium;
import org.seitmolabs.modules.catalog.movie.domain.Movie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Entity
@Table(
        name = "screenings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_screening_auditorium_start",
                columnNames = {"auditorium_id", "starts_at"}
        )
)
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditorium_id", nullable = false)
    private Auditorium auditorium;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ScreeningStatus status;
}
