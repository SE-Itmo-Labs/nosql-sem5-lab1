package org.seitmolabs.modules.consistency.dto;

public record WriteResponse(
        String key,
        String value,
        String writeMode,
        Long replicasAcked,
        long elapsedMs
) {
}
