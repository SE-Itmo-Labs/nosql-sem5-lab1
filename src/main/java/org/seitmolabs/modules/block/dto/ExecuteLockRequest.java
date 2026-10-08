package org.seitmolabs.modules.block.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ExecuteLockRequest(
        @NotBlank
        String resourceKey,

        @Min(0)
        @Max(10000)
        long holdMillis
) {
}
