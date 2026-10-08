package org.seitmolabs.modules.consistency.dto;

public record ReadResponse(
        String key,
        String value,
        String readMode,
        String sourceNode
) {
}
