package org.seitmolabs.modules.consistency.dto;

import java.util.List;

/** A point-in-time observation; matching values do not measure replication lag. */
public record HoldReplicationResponse(String holdId, List<Node> nodes) {
    public record Node(String node, String role, String status, Long ttlSeconds) {}
}
