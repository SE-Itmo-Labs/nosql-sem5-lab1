package org.seitmolabs.modules.consistency.enums;

import org.seitmolabs.common.exceptions.BadRequestException;

public enum ExperimentReadTarget {
    PRIMARY,
    REPLICA;

    public static ExperimentReadTarget from(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException(
                    "Недопустимое значение readTarget: " + value + ". Допустимо: PRIMARY, REPLICA"
            );
        }
    }
}
