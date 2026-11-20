package org.seitmolabs.modules.consistency.topology;

public record RedisNodeId(String value) {

    public RedisNodeId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Идентификатор Redis-узла обязателен");
        }
        value = value.trim();
    }
}
