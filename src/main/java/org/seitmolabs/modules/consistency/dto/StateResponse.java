package org.seitmolabs.modules.consistency.dto;

public record StateResponse(
        String readMode,
        String writeMode,
        long waitTimeoutMs,
        int waitReplicas
) {
}
