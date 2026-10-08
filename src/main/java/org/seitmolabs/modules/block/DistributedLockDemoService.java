package org.seitmolabs.modules.block;

import org.seitmolabs.modules.block.dto.ExecuteLockResponse;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DistributedLockDemoService {

    private final DistributedLockService lockService;

    public ExecuteLockResponse execute(String resourceKey, String owner, long holdMillis) {
        boolean acquired = lockService.executeWithLock(
                resourceKey,
                () -> holdLock(holdMillis)
        );

        String message = acquired
                ? "Критическая секция выполнена"
                : "Ресурс уже обрабатывается другим запросом";

        return new ExecuteLockResponse(resourceKey, owner, acquired, message);
    }

    private void holdLock(long holdMillis) {
        try {
            Thread.sleep(holdMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Выполнение критической секции прервано", e);
        }
    }
}
