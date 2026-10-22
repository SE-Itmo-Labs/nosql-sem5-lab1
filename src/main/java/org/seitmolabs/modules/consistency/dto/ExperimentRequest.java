package org.seitmolabs.modules.consistency.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExperimentRequest(
        @NotNull
        Long screeningId,

        @NotBlank
        String readTarget,

        @NotBlank
        String mode,

        @Min(1)
        @Max(5000)
        long waitTimeoutMs
) {
}
