package org.seitmolabs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * DistributedLockService — распределённая блокировка ресурса.
 * <p>
 * Назначение: гарантировать, что ресурс обрабатывает только один владелец.
 * Идея реализации:
 * <ul>
 *   <li>захват: {@code SET lock:{resourceId} <token> NX EX <timeout>} — атомарно,
 *       занимает ключ, только если он свободен (NX);</li>
 *   <li>снятие: Lua-скрипт GET+DEL по своему токену, чтобы нельзя было
 *       снять чужую блокировку.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private static final String KEY_PREFIX = "lock:";

    private final StringRedisTemplate redis;

    /**
     * Попытаться захватить блокировку.
     *
     * @return true — блокировка захвачена, false — уже занята другим владельцем
     */
    public boolean tryLock(String resourceId, String token, long ttlSeconds) {
        throw new UnsupportedOperationException("TODO: setIfAbsent(KEY_PREFIX + resourceId, token, Duration.ofSeconds(ttlSeconds))");
    }

    /** Снять блокировку по своему токену (чужая не снимется — Lua GET + DEL). */
    public boolean unlock(String resourceId, String token) {
        throw new UnsupportedOperationException("TODO: redis.execute(RELEASE_SCRIPT, List.of(KEY_PREFIX + resourceId), token)");
    }
}