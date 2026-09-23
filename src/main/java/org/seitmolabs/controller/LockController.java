package org.seitmolabs.controller;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.service.DistributedLockService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * LockController — REST API для демонстрации распределённой блокировки.
 * <p>
 * Назначение: показать атомарный механизм. Два параллельных запроса
 * на один resourceId — только один получит true, второй — false. Эндпоинты:
 * <ul>
 *   <li>POST /api/locks/try — захватить блокировку (SET NX EX);</li>
 *   <li>POST /api/locks/release — снять блокировку по своему токену.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/locks")
@RequiredArgsConstructor
public class LockController {

    private final DistributedLockService service;

    @PostMapping("/try")
    public Map<String, Object> tryLock(
            @RequestParam String resourceId,
            @RequestParam String token,
            @RequestParam(defaultValue = "10") long ttlSeconds) {

        throw new UnsupportedOperationException("TODO: service.tryLock(...) → { resourceId, acquired, message }");
    }

    @PostMapping("/release")
    public Map<String, Object> release(
            @RequestParam String resourceId,
            @RequestParam String token) {

        throw new UnsupportedOperationException("TODO: service.unlock(...) → { resourceId, released, message }");
    }
}