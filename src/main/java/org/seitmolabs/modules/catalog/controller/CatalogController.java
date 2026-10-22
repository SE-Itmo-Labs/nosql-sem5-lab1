package org.seitmolabs.modules.catalog.controller;

import java.util.List;

import org.seitmolabs.modules.catalog.dto.MovieResponse;
import org.seitmolabs.modules.catalog.dto.ScreeningResponse;
import org.seitmolabs.modules.catalog.dto.ScreeningSeatsResponse;
import org.seitmolabs.modules.catalog.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Cinema catalog", description = "Афиша, сеансы и схема залов")
public class CatalogController {

    private final CatalogService catalogService;

    @Operation(summary = "Получить актуальную афишу")
    @GetMapping("/movies")
    public List<MovieResponse> getMovies() {
        return catalogService.getMovies();
    }

    @Operation(summary = "Получить карточку фильма")
    @GetMapping("/movies/{movieId}")
    public MovieResponse getMovie(@PathVariable Long movieId) {
        return catalogService.getMovie(movieId);
    }

    @Operation(summary = "Получить будущие сеансы фильма")
    @GetMapping("/movies/{movieId}/screenings")
    public List<ScreeningResponse> getMovieScreenings(@PathVariable Long movieId) {
        return catalogService.getMovieScreenings(movieId);
    }

    @Operation(summary = "Получить схему мест для сеанса")
    @GetMapping("/screenings/{screeningId}/seats")
    public ScreeningSeatsResponse getScreeningSeats(@PathVariable Long screeningId) {
        return catalogService.getScreeningSeats(screeningId);
    }
}
