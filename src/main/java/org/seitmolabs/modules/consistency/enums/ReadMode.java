package org.seitmolabs.modules.consistency.enums;

import org.seitmolabs.common.exceptions.BadRequestException;

public enum ReadMode {

    MASTER,

    REPLICA_PREFERRED;

    public static ReadMode from(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException(
                    "Недопустимое значение readMode: " + value + ". Допустимо: MASTER, REPLICA_PREFERRED");
        }
    }
}
