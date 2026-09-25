package org.seitmolabs.repository;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Block;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BlockRepository {

    private static final String BLOCK_KEY_PREFIX = "block:";

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    /** Атомарно создаёт блокировку с TTL; возвращает false, если ключ уже занят. */
    public boolean trySave(Block block) {
        try {
            String jsonBlock = jsonMapper.writeValueAsString(block);

            Boolean saved = redis.opsForValue().setIfAbsent(
                    BLOCK_KEY_PREFIX + block.getResourceKey(),
                    jsonBlock,
                    Duration.ofSeconds(block.getTtlSeconds())
            );

            return Boolean.TRUE.equals(saved);
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Не удалось сериализовать временную блокировку ресурса "
                            + block.getResourceKey(),
                    e
            );
        }
    }

}