package org.seitmolabs.modules.booking.hold.dto;

public record ExperimentalHoldResponse(SeatHoldResponse hold, HoldReplicationMode mode,
        Long replicasAcked, long waitDurationMs, String waitStatus) {}
