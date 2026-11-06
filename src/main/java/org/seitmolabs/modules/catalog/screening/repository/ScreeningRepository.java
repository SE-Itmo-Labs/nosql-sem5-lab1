package org.seitmolabs.modules.catalog.screening.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.screening.domain.ScreeningStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    @EntityGraph(attributePaths = {"movie", "auditorium"})
    List<Screening> findAllByMovieIdAndStartsAtAfterAndStatusOrderByStartsAtAsc(
            Long movieId,
            Instant startsAfter,
            ScreeningStatus status
    );

    @Override
    @EntityGraph(attributePaths = {"movie", "auditorium"})
    Optional<Screening> findById(Long id);

    boolean existsByAuditoriumIdAndStartsAt(Long auditoriumId, Instant startsAt);
}
