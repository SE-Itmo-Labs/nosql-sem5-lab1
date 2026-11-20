package org.seitmolabs.modules.catalog.movie.repository;

import java.util.List;
import java.util.Optional;

import org.seitmolabs.modules.catalog.movie.domain.Movie;
import org.seitmolabs.modules.catalog.movie.domain.MovieStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MovieRepository extends MongoRepository<Movie, Long> {

    List<Movie> findAllByStatusNotOrderByTitleAsc(MovieStatus status);

    Optional<Movie> findBySlug(String slug);
}
