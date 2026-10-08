package org.seitmolabs.modules.consistency.dto;

public record ModeRequest(
        String readMode,
        String writeMode
) {
}
