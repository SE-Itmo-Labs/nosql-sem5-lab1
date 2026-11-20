package org.seitmolabs.modules.booking.order.dto;

import java.math.BigDecimal;

public record OrderProductResponse(
        Long productId,
        String name,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
