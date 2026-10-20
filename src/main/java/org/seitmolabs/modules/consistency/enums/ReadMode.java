package org.seitmolabs.modules.consistency.enums;

import org.seitmolabs.common.exceptions.BadRequestException;

public enum ReadMode {

    PRIMARY,

    REPLICA_PREFERRED;

    public static ReadMode from(String value) {
        try {
            String normalized = value.toUpperCase();
            return "MASTER".equals(normalized) ? PRIMARY : valueOf(normalized);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException(
                    "Недопустимое значение readMode: " + value + ". Допустимо: PRIMARY, REPLICA_PREFERRED");
        }
    }
}
