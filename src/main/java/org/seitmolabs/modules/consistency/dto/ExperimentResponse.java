package org.seitmolabs.modules.consistency.dto;

public record ExperimentResponse(
        String resourceType,
        Long resourceId,
        String resourceLabel,
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
