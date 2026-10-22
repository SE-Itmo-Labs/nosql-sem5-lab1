package org.seitmolabs.modules.consistency.dto;

public record NodeInfoResponse(
        String node,
        String value,
        Long ttlSeconds,
        String role,
        String linkUp
) {
}
