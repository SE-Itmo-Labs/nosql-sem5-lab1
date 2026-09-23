package org.seitmolabs.controller;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Block;
import org.seitmolabs.service.BlockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * BlockController — REST API сценария TTL: временная блокировка ресурса.
 * <p>
 * Назначение: HTTP-обёртка над BlockService. Эндпоинты:
 * <ul>
 *   <li>POST /api/blocks — заблокировать ресурс на ttlSeconds;</li>
 *   <li>GET /api/blocks/{resourceId} — активна ли блокировка (200 — да, 404 — истекла).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService service;

    @PostMapping
    public ResponseEntity<Block> create(
            @RequestParam String resourceId,
            @RequestParam String owner,
            @RequestParam(defaultValue = "300") long ttlSeconds) {

        throw new UnsupportedOperationException("TODO: service.block(...) → 201 Created + тело");
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<Block> getActive(@PathVariable String resourceId) {
        throw new UnsupportedOperationException("TODO: service.getActive(resourceId) → 200 / 404");
    }
}