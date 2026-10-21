package org.seitmolabs.modules.consistency.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.seitmolabs.common.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;

/** Owns all consistency-experiment clients and hides topology size from services. */
@Component
@RequiredArgsConstructor
public class RedisNodeRegistry {

    private final RedisTopologyProperties properties;
    private final RedisNodeClientFactory clientFactory;

    private Map<RedisNodeId, RedisNodeClient> clients = Map.of();
    private RedisNodeClient primary;

    @PostConstruct
    void connect() {
        Map<RedisNodeId, RedisNodeClient> created = new LinkedHashMap<>();
        List<RedisNodeClient> createdClients = new ArrayList<>();

        try {
            for (RedisTopologyProperties.Node definition : properties.getNodes()) {
                validateDefinition(definition);
                RedisNodeClient client = clientFactory.create(definition);
                if (created.putIfAbsent(client.id(), client) != null) {
                    client.close();
                    throw new IllegalStateException("Повторяется Redis nodeId: " + client.id().value());
                }
                createdClients.add(client);
            }

            List<RedisNodeClient> primaries = createdClients.stream()
                    .filter(client -> client.role() == RedisNodeRole.PRIMARY)
                    .toList();
            if (primaries.size() != 1) {
                throw new IllegalStateException("Redis-топология должна содержать ровно один PRIMARY");
            }

            primary = primaries.getFirst();
            clients = Collections.unmodifiableMap(created);
        } catch (RuntimeException exception) {
            createdClients.forEach(RedisNodeClient::close);
            throw exception;
        }
    }

    @PreDestroy
    void disconnect() {
        clients.values().forEach(RedisNodeClient::close);
    }

    public RedisNodeClient primary() {
        return primary;
    }

    public List<RedisNodeClient> replicas() {
        return clients.values().stream()
                .filter(client -> client.role() == RedisNodeRole.REPLICA)
                .toList();
    }

    public List<RedisNodeClient> all() {
        return List.copyOf(clients.values());
    }

    public RedisNodeClient get(String nodeId) {
        RedisNodeClient client = clients.get(new RedisNodeId(nodeId));
        if (client == null) {
            throw new BadRequestException("Неизвестный Redis-узел: " + nodeId);
        }
        return client;
    }

    public RedisNodeClient requireReplica(String nodeId) {
        RedisNodeClient client = get(nodeId);
        if (client.role() != RedisNodeRole.REPLICA) {
            throw new BadRequestException("Операция доступна только для реплики: " + nodeId);
        }
        return client;
    }

    private void validateDefinition(RedisTopologyProperties.Node definition) {
        if (definition.getRole() == null
                || definition.getHost() == null
                || definition.getHost().isBlank()
                || definition.getPort() <= 0) {
            throw new IllegalStateException("Redis-узел настроен не полностью: " + definition.getId());
        }
    }
}
