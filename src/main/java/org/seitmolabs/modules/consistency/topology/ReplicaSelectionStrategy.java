package org.seitmolabs.modules.consistency.topology;

public interface ReplicaSelectionStrategy {

    RedisNodeClient select();
}
