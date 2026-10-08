package org.seitmolabs.modules.consistency.dto;

public record ExperimentResponse(
        String key,
        String writtenValue,
        String readValue,
        String readTarget,
        String sourceNode,
        String mode,
        Long replicasAcked,
        boolean consistent,
        long writeDurationMs,
        long waitDurationMs,
        long readDurationMs
) {
}
