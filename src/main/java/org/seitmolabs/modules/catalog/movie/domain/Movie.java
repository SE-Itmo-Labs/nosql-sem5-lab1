package org.seitmolabs.modules.catalog.movie.domain;

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
@Document(collection = "movies")
public class Movie {
    @Id
    private Long id;

    private String slug;
    private String title;
    private String description;
    private int durationMinutes;
    private int ageRating;
    private MovieStatus status;
}
