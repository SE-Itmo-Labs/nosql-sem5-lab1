package org.seitmolabs.modules.block.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateBlockRequest(
        @NotBlank
        String resourceKey,

        @NotBlank
        String owner,

        @Positive
        long ttlSeconds
) {
}
