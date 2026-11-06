package org.seitmolabs.modules.booking.hold.factory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.seitmolabs.modules.booking.hold.domain.SeatHold;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SeatHoldFactory {

    private final Duration holdDuration;
    private final Clock clock;

    @Autowired
    public SeatHoldFactory(@Value("${app.booking.hold-duration}") Duration holdDuration) {
        this(holdDuration, Clock.systemUTC());
    }

    public SeatHold create(Long userId, Long screeningId, List<Long> seatIds) {
        Instant createdAt = Instant.now(clock);
        return new SeatHold(
                UUID.randomUUID().toString(),
                userId,
                screeningId,
                List.copyOf(seatIds),
                createdAt,
                createdAt.plus(holdDuration)
        );
    }

    public Duration holdDuration() {
        return holdDuration;
    }
}
