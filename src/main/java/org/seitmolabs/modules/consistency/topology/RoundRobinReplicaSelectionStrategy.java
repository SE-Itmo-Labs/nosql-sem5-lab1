package org.seitmolabs.modules.consistency.topology;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoundRobinReplicaSelectionStrategy implements ReplicaSelectionStrategy {

    private final RedisNodeRegistry registry;
    private final AtomicInteger nextIndex = new AtomicInteger();

    @Override
    public RedisNodeClient select() {
        List<RedisNodeClient> replicas = registry.replicas();
        if (replicas.isEmpty()) {
            throw new IllegalStateException("В Redis-топологии нет реплик");
        }

        int index = Math.floorMod(nextIndex.getAndIncrement(), replicas.size());
        return replicas.get(index);
    }
}
