package org.seitmolabs.modules.catalog.seat.repository;

import java.util.List;

import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByAuditoriumIdOrderByRowNumberAscSeatNumberAsc(Long auditoriumId);

    @EntityGraph(attributePaths = "auditorium")
    List<Seat> findAllByIdIn(List<Long> ids);

    boolean existsByAuditoriumId(Long auditoriumId);
}
