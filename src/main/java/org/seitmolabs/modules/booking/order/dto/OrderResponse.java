package org.seitmolabs.modules.booking.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String status,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant confirmedAt,
        List<TicketResponse> tickets,
        List<OrderProductResponse> products
) {
}
