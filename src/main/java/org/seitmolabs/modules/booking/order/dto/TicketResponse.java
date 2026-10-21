package org.seitmolabs.modules.booking.order.dto;

import java.math.BigDecimal;

public record TicketResponse(
        Long id,
        Long screeningId,
        Long seatId,
        int rowNumber,
        int seatNumber,
        BigDecimal price,
        String status
) {
}
