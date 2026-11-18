package org.seitmolabs.modules.catalog.screening.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.screening.domain.ScreeningStatus;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ScreeningRepository extends MongoRepository<Screening, Long> {

    @Query(value = "{ 'movie': ?0, 'startsAt': { '$gt': ?1 }, 'status': ?2 }", sort = "{ 'startsAt': 1 }")
    List<Screening> findAllByMovieIdAndStartsAtAfterAndStatusOrderByStartsAtAsc(
            Long movieId,
            Instant startsAfter,
            ScreeningStatus status
    );

    @Override
    Optional<Screening> findById(Long id);

    @Query(value = "{ 'auditorium': ?0, 'startsAt': ?1 }", exists = true)
    boolean existsByAuditoriumIdAndStartsAt(Long auditoriumId, Instant startsAt);
}
