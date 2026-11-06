package org.seitmolabs.modules.consistency.service;

import java.util.Properties;
import java.util.function.Consumer;

import org.seitmolabs.modules.consistency.dto.ReplicaActionResponse;
import org.seitmolabs.modules.consistency.topology.RedisNodeClient;
import org.seitmolabs.modules.consistency.topology.RedisNodeRegistry;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/** Performs explicit laboratory topology mutations on configured replicas. */
@Service
@RequiredArgsConstructor
public class ReplicationTopologyService {

    private final RedisNodeRegistry registry;

    public ReplicaActionResponse detach(String nodeId) {
        RedisNodeClient replica = registry.requireReplica(nodeId);
        execute(replica, connection -> connection.serverCommands().replicaOfNoOne());
        return state(replica, "detach");
    }

    public ReplicaActionResponse attach(String nodeId) {
        RedisNodeClient replica = registry.requireReplica(nodeId);
        RedisNodeClient primary = registry.primary();
        execute(replica, connection -> connection.serverCommands().replicaOf(
                primary.host(),
                primary.port()
        ));

        ReplicaActionResponse state = state(replica, "attach");
        for (int attempt = 0; attempt < 32 && !"up".equals(state.linkUp()); attempt++) {
            waitBeforeProbe();
            state = state(replica, "attach");
        }
        return state;
    }

    private ReplicaActionResponse state(RedisNodeClient replica, String action) {
        Properties replication = info(replica, "replication");
        return new ReplicaActionResponse(
                replica.id().value(),
                action,
                replication.getProperty("role", "unknown"),
                replication.getProperty("master_link_status")
        );
    }

    private void execute(RedisNodeClient node, Consumer<RedisConnection> action) {
        RedisConnection connection = node.connectionFactory().getConnection();
        try {
            action.accept(connection);
        } finally {
            connection.close();
        }
    }

    private Properties info(RedisNodeClient node, String section) {
        RedisConnection connection = node.connectionFactory().getConnection();
        try {
            return connection.info(section);
        } finally {
            connection.close();
        }
    }

    private void waitBeforeProbe() {
        try {
            Thread.sleep(250);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Ожидание подключения реплики прервано", exception);
        }
    }
}
