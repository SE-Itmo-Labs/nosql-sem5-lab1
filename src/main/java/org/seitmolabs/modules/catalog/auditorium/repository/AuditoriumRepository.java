package org.seitmolabs.modules.catalog.auditorium.repository;

import java.util.Optional;

import org.seitmolabs.modules.catalog.auditorium.domain.Auditorium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriumRepository extends JpaRepository<Auditorium, Long> {

    Optional<Auditorium> findByName(String name);
}
