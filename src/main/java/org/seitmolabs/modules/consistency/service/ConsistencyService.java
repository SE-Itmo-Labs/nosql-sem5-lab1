package org.seitmolabs.modules.consistency.service;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import org.seitmolabs.modules.consistency.dto.ExperimentRequest;
import org.seitmolabs.modules.consistency.dto.ExperimentResponse;
import org.seitmolabs.modules.consistency.dto.ModeRequest;
import org.seitmolabs.modules.consistency.dto.NodesResponse;
import org.seitmolabs.modules.consistency.dto.ReadResponse;
import org.seitmolabs.modules.consistency.dto.ReplicaActionResponse;
import org.seitmolabs.modules.consistency.dto.StateResponse;
import org.seitmolabs.modules.consistency.dto.WriteRequest;
import org.seitmolabs.modules.consistency.dto.WriteResponse;
import org.seitmolabs.modules.consistency.enums.ReadMode;
import org.seitmolabs.modules.consistency.enums.WriteMode;
import org.seitmolabs.modules.consistency.enums.ExperimentMode;
import org.seitmolabs.modules.consistency.enums.ExperimentReadTarget;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ConsistencyService {

    private static final String KEY_PREFIX = "consistency:";

    private final StringRedisTemplate primaryTemplate;
    private final NodeProbeService nodeProbeService;
    private final AtomicInteger nextReplica = new AtomicInteger();
    private final long waitTimeoutMs;
    private final int waitReplicas;

    private volatile ReadMode readMode = ReadMode.MASTER;
    private volatile WriteMode writeMode = WriteMode.ASYNC;

    public ConsistencyService(
            StringRedisTemplate primaryTemplate,
            NodeProbeService nodeProbeService,
            @Value("${app.replication.wait-timeout-ms}") long waitTimeoutMs,
            @Value("${app.replication.wait-replicas}") int waitReplicas
    ) {
        this.primaryTemplate = primaryTemplate;
        this.nodeProbeService = nodeProbeService;
        this.waitTimeoutMs = waitTimeoutMs;
        this.waitReplicas = waitReplicas;
    }

    public StateResponse getState() {
        return new StateResponse(readMode.name(), writeMode.name(), waitTimeoutMs, waitReplicas);
    }

    public StateResponse changeMode(ModeRequest request) {
        ReadMode newReadMode = request.readMode() == null
                ? readMode
                : ReadMode.from(request.readMode());
        WriteMode newWriteMode = request.writeMode() == null
                ? writeMode
                : WriteMode.from(request.writeMode());

        readMode = newReadMode;
        writeMode = newWriteMode;
        return getState();
    }

    public WriteResponse write(WriteRequest request) {
        long startedAt = System.nanoTime();
        Long replicasAcked = null;

        if (writeMode == WriteMode.WAIT_FOR_REPLICAS) {
            replicasAcked = writeAndWait(
                    storageKey(request.key()),
                    request.value(),
                    waitReplicas,
                    waitTimeoutMs
            ).replicasAcked();
        } else {
            primaryTemplate.opsForValue().set(storageKey(request.key()), request.value());
        }

        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        return new WriteResponse(
                request.key(),
                request.value(),
                writeMode.name(),
                replicasAcked,
                elapsedMs
        );
    }

    public ReadResponse read(String key) {
        String redisKey = storageKey(key);

        if (readMode == ReadMode.MASTER) {
            String value = primaryTemplate.opsForValue().get(redisKey);
            return new ReadResponse(key, value, readMode.name(), NodeProbeService.PRIMARY);
        }

        String node = nextReplica.getAndIncrement() % 2 == 0
                ? NodeProbeService.REPLICA_1
                : NodeProbeService.REPLICA_2;

        try {
            String value = nodeProbeService.read(node, redisKey);
            return new ReadResponse(key, value, readMode.name(), node);
        } catch (RuntimeException ex) {
            String value = primaryTemplate.opsForValue().get(redisKey);
            return new ReadResponse(key, value, readMode.name(), NodeProbeService.PRIMARY);
        }
    }

    public NodesResponse getNodes(String key) {
        NodesResponse result = nodeProbeService.probeAll(storageKey(key));
        return new NodesResponse(key, result.nodes());
    }

    public ReplicaActionResponse detachReplica(int replicaNumber) {
        return nodeProbeService.detach(replicaNumber);
    }

    public ReplicaActionResponse attachReplica(int replicaNumber) {
        return nodeProbeService.attach(replicaNumber);
    }

    public ExperimentResponse runExperiment(ExperimentRequest request) {
        ExperimentMode mode = ExperimentMode.from(request.mode());
        ExperimentReadTarget readTarget = ExperimentReadTarget.from(request.readTarget());
        String redisKey = storageKey(request.key());

        long writeDurationMs;
        long waitDurationMs = 0;
        Long replicasAcked = null;

        if (mode == ExperimentMode.WAIT_FOR_REPLICA) {
            WaitResult result = writeAndWait(redisKey, request.value(), 1, request.waitTimeoutMs());
            writeDurationMs = result.writeDurationMs();
            waitDurationMs = result.waitDurationMs();
            replicasAcked = result.replicasAcked();
        } else {
            long writeStartedAt = System.nanoTime();
            primaryTemplate.opsForValue().set(redisKey, request.value());
            writeDurationMs = elapsedMs(writeStartedAt);
        }

        long readStartedAt = System.nanoTime();
        String sourceNode;
        String readValue;

        if (readTarget == ExperimentReadTarget.PRIMARY) {
            sourceNode = NodeProbeService.PRIMARY;
            readValue = primaryTemplate.opsForValue().get(redisKey);
        } else {
            sourceNode = NodeProbeService.REPLICA_1;
            readValue = nodeProbeService.read(sourceNode, redisKey);
        }

        long readDurationMs = elapsedMs(readStartedAt);
        return new ExperimentResponse(
                request.key(),
                request.value(),
                readValue,
                readTarget.name(),
                sourceNode,
                mode.name(),
                replicasAcked,
                Objects.equals(request.value(), readValue),
                writeDurationMs,
                waitDurationMs,
                readDurationMs
        );
    }

    private WaitResult writeAndWait(String key, String value, int replicas, long timeoutMs) {
        return primaryTemplate.execute((RedisConnection connection) -> {
            @SuppressWarnings("unchecked")
            RedisClusterAsyncCommands<byte[], byte[]> commands =
                    (RedisClusterAsyncCommands<byte[], byte[]>) connection.getNativeConnection();

            try {
                long writeStartedAt = System.nanoTime();
                commands.set(bytes(key), bytes(value)).get();
                long writeDurationMs = elapsedMs(writeStartedAt);

                long waitStartedAt = System.nanoTime();
                Long replicasAcked = commands.waitForReplication(replicas, timeoutMs).get();
                long waitDurationMs = elapsedMs(waitStartedAt);

                return new WaitResult(replicasAcked, writeDurationMs, waitDurationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Ожидание реплик было прервано", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Не удалось дождаться реплик", e.getCause());
            }
        });
    }

    private String storageKey(String key) {
        return KEY_PREFIX + key;
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private record WaitResult(
            Long replicasAcked,
            long writeDurationMs,
            long waitDurationMs
    ) {
    }
}
