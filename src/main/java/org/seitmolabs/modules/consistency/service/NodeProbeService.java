package org.seitmolabs.modules.consistency.service;

import java.util.List;
import java.util.Properties;

import org.seitmolabs.modules.consistency.dto.NodeInfoResponse;
import org.seitmolabs.modules.consistency.dto.NodesResponse;
import org.seitmolabs.modules.consistency.topology.RedisNodeClient;
import org.seitmolabs.modules.consistency.topology.RedisNodeRegistry;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Reads diagnostic data without knowing topology names or replica count. */
@Component
@RequiredArgsConstructor
public class NodeProbeService {

    private final RedisNodeRegistry registry;

    public NodeInfoResponse probe(String nodeId, String key) {
        RedisNodeClient node = registry.get(nodeId);
        StringRedisTemplate template = node.template();

        String value = template.opsForValue().get(key);
        Long ttlSeconds = template.getExpire(key);
        Properties replication = info(template, "replication");

        return new NodeInfoResponse(
                node.id().value(),
                value,
                ttlSeconds,
                replication.getProperty("role", "unknown"),
                replication.getProperty("master_link_status")
        );
    }

    public NodesResponse probeAll(String key) {
        List<NodeInfoResponse> nodes = registry.all().stream()
                .map(node -> probe(node.id().value(), key))
                .toList();
        return new NodesResponse(key, nodes);
    }

    public String read(String nodeId, String key) {
        return registry.get(nodeId).template().opsForValue().get(key);
    }

    private Properties info(StringRedisTemplate template, String section) {
        RedisConnection connection = template.getConnectionFactory().getConnection();
        try {
            return connection.info(section);
        } finally {
            connection.close();
        }
    }
}
