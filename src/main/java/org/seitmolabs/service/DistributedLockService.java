package org.seitmolabs.service;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private static final String LOCK_KEY_PREFIX = "lock:";

    private final RedissonClient redissonClient;

    public boolean executeWithLock(
            String resourceKey,
            Runnable criticalSection
    ) {
        RLock lock = redissonClient.getLock(
                LOCK_KEY_PREFIX + resourceKey
        );

        boolean lockAcquired = lock.tryLock();

        if (!lockAcquired) {
            return false;
        }

        try {
            criticalSection.run();
            return true;
        } finally {
            lock.unlock();
        }
    }
}