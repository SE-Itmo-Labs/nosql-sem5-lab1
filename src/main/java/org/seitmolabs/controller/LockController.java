package org.seitmolabs.controller;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.service.DistributedLockService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/locks")
@RequiredArgsConstructor
public class LockController {

    private final DistributedLockService service;

    @PostMapping("/test")
    public Map<String, Object> testLock(
            @RequestParam String resourceKey,
            @RequestParam(defaultValue = "3000") long holdMillis
    ) {
        boolean acquired = service.executeWithLock(
                resourceKey,
                () -> holdLock(holdMillis)
        );

        return Map.of(
                "resourceKey", resourceKey,
                "acquired", acquired
        );
    }

    private void holdLock(long holdMillis) {
        try {
            Thread.sleep(holdMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Ожидание было прервано",
                    e
            );
        }
    }
}