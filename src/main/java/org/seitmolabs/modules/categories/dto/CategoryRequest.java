package org.seitmolabs.modules.categories.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank
        String name,

        @NotBlank
        String description
) {
}
