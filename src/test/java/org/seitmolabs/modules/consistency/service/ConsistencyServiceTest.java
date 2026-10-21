package org.seitmolabs.modules.consistency.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.seitmolabs.modules.catalog.auditorium.domain.Auditorium;
import org.seitmolabs.modules.catalog.movie.domain.Movie;
import org.seitmolabs.modules.catalog.movie.domain.MovieStatus;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.screening.domain.ScreeningStatus;
import org.seitmolabs.modules.catalog.screening.repository.ScreeningRepository;
import org.seitmolabs.modules.consistency.dto.ExperimentRequest;
import org.seitmolabs.modules.consistency.dto.ExperimentResponse;
import org.seitmolabs.modules.consistency.topology.RedisNodeClient;
import org.seitmolabs.modules.consistency.topology.RedisNodeId;
import org.seitmolabs.modules.consistency.topology.RedisNodeRegistry;
import org.seitmolabs.modules.consistency.topology.ReplicaSelectionStrategy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class ConsistencyServiceTest {

    @SuppressWarnings("unchecked")
    @Test
    void experimentsWithProjectionOfExistingScreening() {
        RedisNodeRegistry registry = mock(RedisNodeRegistry.class);
        RedisNodeClient primary = mock(RedisNodeClient.class);
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        ScreeningRepository screenings = mock(ScreeningRepository.class);
        Movie movie = Movie.builder().id(7L).title("Северный свет").slug("north")
                .description("Описание").durationMinutes(100).ageRating(12)
                .status(MovieStatus.NOW_SHOWING).build();
        Screening screening = Screening.builder().id(11L).movie(movie)
                .auditorium(Auditorium.builder().id(2L).name("Зал 1").active(true).build())
                .startsAt(Instant.parse("2026-10-22T12:00:00Z"))
                .status(ScreeningStatus.SCHEDULED).build();
        String projection = "screening=11|movie=7|status=SCHEDULED|startsAt=2026-10-22T12:00:00Z";
        when(screenings.findById(11L)).thenReturn(Optional.of(screening));
        when(registry.primary()).thenReturn(primary);
        when(primary.id()).thenReturn(new RedisNodeId("primary"));
        when(primary.template()).thenReturn(template);
        when(template.opsForValue()).thenReturn(values);
        when(values.get("laboratory:screening:11")).thenReturn(projection);

        ConsistencyService service = new ConsistencyService(registry, mock(NodeProbeService.class),
                mock(ReplicaSelectionStrategy.class), mock(ReplicationTopologyService.class), screenings);
        ExperimentResponse response = service.runExperiment(
                new ExperimentRequest(11L, "PRIMARY", "EVENTUAL", 500));

        verify(values).set("laboratory:screening:11", projection);
        assertThat(response.resourceType()).isEqualTo("SCREENING");
        assertThat(response.resourceId()).isEqualTo(11L);
        assertThat(response.consistent()).isTrue();
    }
}
