package org.seitmolabs.modules.booking.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotBlank String holdId,
        @Valid @Size(max = 20) List<OrderProductRequest> products,
        @NotBlank @Size(max = 100) String idempotencyKey
) {

    public CreateOrderRequest {
        products = products == null ? List.of() : List.copyOf(products);
    }
}
