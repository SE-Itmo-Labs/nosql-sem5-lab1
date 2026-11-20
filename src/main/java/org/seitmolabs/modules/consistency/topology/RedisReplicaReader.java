package org.seitmolabs.modules.consistency.topology;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

//Читать из реплики

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisReplicaReader {

    private final RedisNodeRegistry redisNodeRegistry;
    private final StringRedisTemplate primaryTemplate;

    private final AtomicInteger nextIndex = new AtomicInteger();

    /**
     * Читает реплики по кругу. При ошибке подключения или таймауте
     * пробует следующий узел, затем primary.
     * Успешный ответ реплики может содержать устаревшие данные.
     */
    public <T> T read(Function<StringRedisTemplate, T> operation) {

        List<RedisNodeClient> replicas = redisNodeRegistry.replicas();

        if (replicas.isEmpty()) {
            return operation.apply(primaryTemplate);
        }

        int start = Math.floorMod(
                nextIndex.getAndIncrement(),
                replicas.size()
        );

        for (int offset = 0; offset < replicas.size(); offset++) {
            RedisNodeClient replica =
                    replicas.get((start + offset) % replicas.size());

        try {
            return operation.apply(replica.template());
        } catch (DataAccessResourceFailureException
                 | QueryTimeoutException exception) {
            log.warn(
                    "Ошибка чтения Redis-реплики {}",
                    replica.id().value(),
                    exception
            );
        }
        }

        return operation.apply(primaryTemplate);
    }
}
