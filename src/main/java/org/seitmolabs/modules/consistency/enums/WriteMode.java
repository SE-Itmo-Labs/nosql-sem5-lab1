package org.seitmolabs.modules.consistency.enums;

import org.seitmolabs.common.exceptions.BadRequestException;

public enum WriteMode {

    ASYNC,

    WAIT_FOR_REPLICAS;

    public static WriteMode from(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException(
                    "Недопустимое значение writeMode: " + value + ". Допустимо: ASYNC, WAIT_FOR_REPLICAS");
        }
    }
}
