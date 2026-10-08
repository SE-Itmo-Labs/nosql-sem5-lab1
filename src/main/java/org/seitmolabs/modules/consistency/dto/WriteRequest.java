package org.seitmolabs.modules.consistency.dto;

import jakarta.validation.constraints.NotBlank;

public record WriteRequest(
        @NotBlank
        String key,

        @NotBlank
        String value
) {
}
