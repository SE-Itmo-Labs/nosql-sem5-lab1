package org.seitmolabs.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNotificationRequest(
        @NotBlank
        String userId,

        @NotBlank
        String title,

        @NotBlank
        String text,

        @NotNull
        Long categoryId
) {
}
