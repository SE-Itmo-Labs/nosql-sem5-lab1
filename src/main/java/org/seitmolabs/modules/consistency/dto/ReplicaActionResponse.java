package org.seitmolabs.modules.consistency.dto;

public record ReplicaActionResponse(
        int node,
        String action,
        String role,
        String linkUp
) {
}
