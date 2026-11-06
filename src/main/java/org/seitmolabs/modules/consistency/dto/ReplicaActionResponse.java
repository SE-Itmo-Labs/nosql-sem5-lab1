package org.seitmolabs.modules.consistency.dto;

public record ReplicaActionResponse(
        String nodeId,
        String action,
        String role,
        String linkUp
) {
}
