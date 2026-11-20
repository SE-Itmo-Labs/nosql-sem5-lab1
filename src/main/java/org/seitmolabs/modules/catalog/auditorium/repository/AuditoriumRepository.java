package org.seitmolabs.modules.catalog.auditorium.repository;

import java.util.Optional;

import org.seitmolabs.modules.catalog.auditorium.domain.Auditorium;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditoriumRepository extends MongoRepository<Auditorium, Long> {

    Optional<Auditorium> findByName(String name);
}
