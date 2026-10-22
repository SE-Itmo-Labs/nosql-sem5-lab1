package org.seitmolabs.modules.categories.dto;

public record CategoryCacheInfo(
        String source,
        boolean cached,
        long remainingTtlSeconds
) {
}
