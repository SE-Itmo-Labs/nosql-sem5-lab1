package org.seitmolabs.modules.consistency.service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import org.seitmolabs.modules.consistency.dto.ModeRequest;
import org.seitmolabs.modules.consistency.dto.NodesResponse;
import org.seitmolabs.modules.consistency.dto.ReadResponse;
import org.seitmolabs.modules.consistency.dto.ReplicaActionResponse;
import org.seitmolabs.modules.consistency.dto.StateResponse;
import org.seitmolabs.modules.consistency.dto.WriteRequest;
import org.seitmolabs.modules.consistency.dto.WriteResponse;
import org.seitmolabs.modules.consistency.enums.ReadMode;
import org.seitmolabs.modules.consistency.enums.WriteMode;
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
            replicasAcked = writeAndWait(storageKey(request.key()), request.value());
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

    private Long writeAndWait(String key, String value) {
        return primaryTemplate.execute((RedisConnection connection) -> {
            @SuppressWarnings("unchecked")
            RedisClusterAsyncCommands<byte[], byte[]> commands =
                    (RedisClusterAsyncCommands<byte[], byte[]>) connection.getNativeConnection();

            try {
                commands.set(bytes(key), bytes(value)).get();
                return commands.waitForReplication(waitReplicas, waitTimeoutMs).get();
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
}
