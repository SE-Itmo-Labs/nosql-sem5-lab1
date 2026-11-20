package org.seitmolabs.modules.consistency.enums;

import org.seitmolabs.common.exceptions.BadRequestException;

public enum ExperimentMode {
    EVENTUAL,
    WAIT_FOR_REPLICA;

    public static ExperimentMode from(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException(
                    "Недопустимое значение mode: " + value + ". Допустимо: EVENTUAL, WAIT_FOR_REPLICA"
            );
        }
    }
}
