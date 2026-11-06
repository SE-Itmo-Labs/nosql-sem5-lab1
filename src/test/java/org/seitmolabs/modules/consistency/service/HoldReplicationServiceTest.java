package org.seitmolabs.modules.consistency.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.seitmolabs.modules.booking.hold.service.SeatHoldService;
import org.seitmolabs.modules.consistency.topology.RedisNodeClient;
import org.seitmolabs.modules.consistency.topology.RedisNodeId;
import org.seitmolabs.modules.consistency.topology.RedisNodeRegistry;
import org.seitmolabs.modules.user.domain.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class HoldReplicationServiceTest {
    private final SeatHoldService holds = mock(SeatHoldService.class);
    private final RedisNodeRegistry registry = mock(RedisNodeRegistry.class);
    private final StringRedisTemplate primary = mock(StringRedisTemplate.class);
    private final User user = mock(User.class);
    private final HoldReplicationService service = new HoldReplicationService(holds, registry, primary);

    @SuppressWarnings("unchecked")
    @Test
    void comparesEachReplicaAndContinuesWhenOneIsUnavailable() {
        ValueOperations<String, String> values = mock(ValueOperations.class);
        RedisNodeClient primaryNode = mock(RedisNodeClient.class);
        when(primary.opsForValue()).thenReturn(values);
        when(values.get("hold:abc")).thenReturn("current");
        when(primary.getExpire("hold:abc")).thenReturn(240L);
        when(registry.primary()).thenReturn(primaryNode);
        when(primaryNode.id()).thenReturn(new RedisNodeId("primary"));

        RedisNodeClient same = replica("same", "current");
        RedisNodeClient missing = replica("missing", null);
        RedisNodeClient stale = replica("stale", "old");
        RedisNodeClient down = mock(RedisNodeClient.class);
        StringRedisTemplate downTemplate = mock(StringRedisTemplate.class);
        when(down.id()).thenReturn(new RedisNodeId("down"));
        when(down.template()).thenReturn(downTemplate);
        when(downTemplate.opsForValue()).thenThrow(new IllegalStateException("offline"));
        when(registry.replicas()).thenReturn(List.of(same, missing, stale, down));

        var result = service.inspect("abc", user);

        assertThat(result.nodes()).extracting("status")
                .containsExactly("PRESENT", "MATCH", "MISSING", "DIFFERENT", "UNAVAILABLE");
        assertThat(result.nodes().getFirst().ttlSeconds()).isEqualTo(240L);
        verify(holds).requireOwned("abc", user);
    }

    @Test
    void doesNotProbeReplicasForAnotherUsersHold() {
        when(holds.requireOwned("abc", user)).thenThrow(new EntityNotFoundException());

        assertThatThrownBy(() -> service.inspect("abc", user)).isInstanceOf(EntityNotFoundException.class);
        verify(registry, never()).replicas();
    }

    @SuppressWarnings("unchecked")
    private RedisNodeClient replica(String id, String value) {
        RedisNodeClient node = mock(RedisNodeClient.class);
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(node.id()).thenReturn(new RedisNodeId(id));
        when(node.template()).thenReturn(template);
        when(template.opsForValue()).thenReturn(values);
        when(values.get("hold:abc")).thenReturn(value);
        return node;
    }
}
