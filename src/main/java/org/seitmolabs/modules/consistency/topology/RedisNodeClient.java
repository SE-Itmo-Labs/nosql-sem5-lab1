package org.seitmolabs.modules.consistency.topology;

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

public record RedisNodeClient(
        RedisNodeId id,
        RedisNodeRole role,
        String host,
        int port,
        LettuceConnectionFactory connectionFactory,
        StringRedisTemplate template
) implements AutoCloseable {

    @Override
    public void close() {
        connectionFactory.destroy();
    }
}
