package org.seitmolabs.modules.block.dto;

import java.time.Instant;

public record BlockResponse(
        String resourceKey,
        String owner,
        long ttlSeconds,
        long remainingTtlSeconds,
        Instant createdAt,
        Instant expiresAt,
        boolean active
) {
}
