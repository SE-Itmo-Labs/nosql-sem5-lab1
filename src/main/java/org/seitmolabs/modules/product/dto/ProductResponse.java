package org.seitmolabs.modules.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        boolean available
) {
}
