package org.seitmolabs.modules.catalog.seat.repository;

import java.util.List;

import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SeatRepository extends MongoRepository<Seat, Long> {

    @Query(value = "{ 'auditorium': ?0 }", sort = "{ 'rowNumber': 1, 'seatNumber': 1 }")
    List<Seat> findAllByAuditoriumIdOrderByRowNumberAscSeatNumberAsc(Long auditoriumId);

    List<Seat> findAllByIdIn(List<Long> ids);

    @Query(value = "{ 'auditorium': ?0 }", exists = true)
    boolean existsByAuditoriumId(Long auditoriumId);
}
