package org.seitmolabs.modules.consistency.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.EntityNotFoundException;

import org.seitmolabs.modules.booking.hold.service.SeatHoldService;
import org.seitmolabs.modules.consistency.dto.HoldReplicationResponse;
import org.seitmolabs.modules.consistency.dto.HoldReplicationResponse.Node;
import org.seitmolabs.modules.consistency.topology.RedisNodeClient;
import org.seitmolabs.modules.consistency.topology.RedisNodeRegistry;
import org.seitmolabs.modules.user.domain.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HoldReplicationService {
    private final SeatHoldService holds;
    private final RedisNodeRegistry registry;
    private final StringRedisTemplate primary;

    public HoldReplicationResponse inspect(String holdId, User user) {
        
        // Authorize against the business primary before touching diagnostic nodes.
        holds.requireOwned(holdId, user);
        String key = "hold:" + holdId;
        String expected = primary.opsForValue().get(key);
        if (expected == null) {
            throw new EntityNotFoundException("Удержание не найдено или истекло: " + holdId);
        }

        List<Node> nodes = new ArrayList<>();
        Long ttl = primary.getExpire(key);
        nodes.add(new Node(registry.primary().id().value(), "PRIMARY", "PRESENT", ttl));
        for (RedisNodeClient replica : registry.replicas()) {
            nodes.add(inspectReplica(replica, key, expected));
        }
        return new HoldReplicationResponse(holdId, nodes);
    }

    private Node inspectReplica(RedisNodeClient replica, String key, String expected) {
        try {
            String value = replica.template().opsForValue().get(key);
            Long ttl = replica.template().getExpire(key);
            String status = value == null ? "MISSING" : Objects.equals(value, expected) ? "MATCH" : "DIFFERENT";
            return new Node(replica.id().value(), "REPLICA", status, ttl);
        } catch (RuntimeException exception) {
            // A single failed replica must not hide the other observations.
            return new Node(replica.id().value(), "REPLICA", "UNAVAILABLE", null);
        }
    }
}
