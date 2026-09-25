package org.seitmolabs.controller;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Block;
import org.seitmolabs.service.BlockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService service;

    @PostMapping
    public ResponseEntity<Block> tryCreateBlock(
            @RequestParam String resourceKey,
            @RequestParam String owner,
            @RequestParam(defaultValue = "300") long ttlSeconds
    ) {
        return service.tryBlock(resourceKey, owner, ttlSeconds)
                .map(block ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(block)
                )
                .orElseGet(() ->
                        ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .build()
                );
    }
}