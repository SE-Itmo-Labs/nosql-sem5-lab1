package org.seitmolabs.modules.booking.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record OrderProductRequest(
        @NotNull Long productId,
        @Positive @Max(20) int quantity
) {
}
