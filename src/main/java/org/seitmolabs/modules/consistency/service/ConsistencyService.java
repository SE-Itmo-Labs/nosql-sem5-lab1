package org.seitmolabs.modules.consistency.service;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.screening.repository.ScreeningRepository;
import org.seitmolabs.modules.consistency.dto.ExperimentRequest;
import org.seitmolabs.modules.consistency.dto.ExperimentResponse;
import org.seitmolabs.modules.consistency.dto.ReplicaActionResponse;
import org.seitmolabs.modules.consistency.enums.ExperimentMode;
import org.seitmolabs.modules.consistency.enums.ExperimentReadTarget;
import org.seitmolabs.modules.consistency.topology.RedisNodeClient;
import org.seitmolabs.modules.consistency.topology.RedisNodeRegistry;
import org.seitmolabs.modules.consistency.topology.ReplicaSelectionStrategy;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsistencyService {
    private static final String KEY_PREFIX = "laboratory:screening:";

    private final RedisNodeRegistry nodeRegistry;
    private final NodeProbeService nodeProbeService;
    private final ReplicaSelectionStrategy replicaSelectionStrategy;
    private final ReplicationTopologyService topologyService;
    private final ScreeningRepository screeningRepository;

    public ReplicaActionResponse detachReplica(String nodeId) {
        return topologyService.detach(nodeId);
    }

    public ReplicaActionResponse attachReplica(String nodeId) {
        return topologyService.attach(nodeId);
    }

    /** Writes an isolated projection of a real screening; PostgreSQL remains authoritative. */
    public ExperimentResponse runExperiment(ExperimentRequest request) {
        Screening screening = screeningRepository.findById(request.screeningId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Сеанс для эксперимента не найден: " + request.screeningId()));
        ExperimentMode mode = ExperimentMode.from(request.mode());
        ExperimentReadTarget readTarget = ExperimentReadTarget.from(request.readTarget());
        String key = KEY_PREFIX + screening.getId();
        String value = projection(screening);

        long writeDurationMs;
        long waitDurationMs = 0;
        Long replicasAcked = null;
        if (mode == ExperimentMode.WAIT_FOR_REPLICA) {
            WaitResult result = writeAndWait(key, value, 1, request.waitTimeoutMs());
            writeDurationMs = result.writeDurationMs();
            waitDurationMs = result.waitDurationMs();
            replicasAcked = result.replicasAcked();
        } else {
            long startedAt = System.nanoTime();
            primaryTemplate().opsForValue().set(key, value);
            writeDurationMs = elapsedMs(startedAt);
        }

        long readStartedAt = System.nanoTime();
        String sourceNode;
        String readValue;
        if (readTarget == ExperimentReadTarget.PRIMARY) {
            RedisNodeClient primary = nodeRegistry.primary();
            sourceNode = primary.id().value();
            readValue = primary.template().opsForValue().get(key);
        } else {
            RedisNodeClient replica = replicaSelectionStrategy.select();
            sourceNode = replica.id().value();
            readValue = nodeProbeService.read(sourceNode, key);
        }

        return new ExperimentResponse("SCREENING", screening.getId(), screening.getMovie().getTitle(),
                key, value, readValue, readTarget.name(), sourceNode, mode.name(), replicasAcked,
                Objects.equals(value, readValue), writeDurationMs, waitDurationMs,
                elapsedMs(readStartedAt));
    }

    private String projection(Screening screening) {
        return "screening=%d|movie=%d|status=%s|startsAt=%s"
                .formatted(screening.getId(), screening.getMovie().getId(),
                        screening.getStatus(), screening.getStartsAt());
    }

    private WaitResult writeAndWait(String key, String value, int replicas, long timeoutMs) {
        return primaryTemplate().execute((RedisConnection connection) -> {
            @SuppressWarnings("unchecked")
            RedisClusterAsyncCommands<byte[], byte[]> commands =
                    (RedisClusterAsyncCommands<byte[], byte[]>) connection.getNativeConnection();
            try {
                long writeStartedAt = System.nanoTime();
                commands.set(bytes(key), bytes(value)).get();
                long writeDurationMs = elapsedMs(writeStartedAt);
                long waitStartedAt = System.nanoTime();
                Long replicasAcked = commands.waitForReplication(replicas, timeoutMs).get();
                return new WaitResult(replicasAcked, writeDurationMs, elapsedMs(waitStartedAt));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Ожидание реплик было прервано", exception);
            } catch (ExecutionException exception) {
                throw new IllegalStateException("Не удалось дождаться реплик", exception.getCause());
            }
        });
    }

    private StringRedisTemplate primaryTemplate() {
        return nodeRegistry.primary().template();
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private record WaitResult(Long replicasAcked, long writeDurationMs, long waitDurationMs) {
    }
}
