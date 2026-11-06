package org.seitmolabs.modules.booking.hold.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSeatHoldRequest(
        @NotNull Long screeningId,
        @NotEmpty @Size(max = 12) List<@NotNull Long> seatIds
) {
}
