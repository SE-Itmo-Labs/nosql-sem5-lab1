package org.seitmolabs.modules.catalog.movie.repository;

import java.util.List;
import java.util.Optional;

import org.seitmolabs.modules.catalog.movie.domain.Movie;
import org.seitmolabs.modules.catalog.movie.domain.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findAllByStatusNotOrderByTitleAsc(MovieStatus status);

    Optional<Movie> findBySlug(String slug);
}
