package org.seitmolabs.modules.consistency.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ExperimentRequest(
        @NotBlank
        String key,

        @NotBlank
        String value,

        @NotBlank
        String readTarget,

        @NotBlank
        String mode,

        @Min(1)
        @Max(5000)
        long waitTimeoutMs
) {
}
