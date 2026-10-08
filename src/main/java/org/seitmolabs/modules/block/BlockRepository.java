package org.seitmolabs.modules.block;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class BlockRepository {

    private static final String BLOCK_KEY_PREFIX = "block:";
    private static final DefaultRedisScript<Long> DELETE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('DEL', KEYS[1]) " +
                    "else return 0 end",
            Long.class
    );

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

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

    public Optional<Block> findByResourceKey(String resourceKey) {
        String jsonBlock = redis.opsForValue().get(BLOCK_KEY_PREFIX + resourceKey);

        if (jsonBlock == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonMapper.readValue(jsonBlock, Block.class));
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Не удалось прочитать блокировку ресурса " + resourceKey,
                    e
            );
        }
    }

    public long getRemainingTtlSeconds(String resourceKey) {
        Long ttl = redis.getExpire(BLOCK_KEY_PREFIX + resourceKey, TimeUnit.SECONDS);
        return ttl == null ? -2 : ttl;
    }

    public boolean delete(Block block) {
        try {
            String jsonBlock = jsonMapper.writeValueAsString(block);
            Long deleted = redis.execute(
                    DELETE_SCRIPT,
                    List.of(BLOCK_KEY_PREFIX + block.getResourceKey()),
                    jsonBlock
            );

            return Long.valueOf(1).equals(deleted);
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Не удалось удалить блокировку ресурса " + block.getResourceKey(),
                    e
            );
        }
    }
}
