package org.seitmolabs.modules.block.dto;

public record ExecuteLockResponse(
        String resourceKey,
        String owner,
        boolean acquired,
        String message
) {
}
