package org.seitmolabs.modules.block.controller;

import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.seitmolabs.modules.block.BlockService;
import org.seitmolabs.modules.block.dto.BlockResponse;
import org.seitmolabs.modules.block.dto.CreateBlockRequest;
import org.seitmolabs.modules.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/blocks")
@RequiredArgsConstructor
@Tag(name = "Temporary blocks", description = "Временная блокировка ресурсов с TTL")
public class BlockController {

    private final BlockService blockService;

    @Operation(summary = "Временно заблокировать ресурс")
    @PostMapping
    public ResponseEntity<BlockResponse> create(@Valid @RequestBody CreateBlockRequest request) {
        BlockResponse block = blockService.create(
                request.resourceKey(),
                request.owner(),
                request.ttlSeconds()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(block);
    }

    @Operation(summary = "Проверить временную блокировку")
    @GetMapping("/{resourceKey}")
    public BlockResponse get(@PathVariable String resourceKey) {
        return blockService.get(resourceKey);
    }

    @Operation(summary = "Досрочно освободить свою блокировку")
    @DeleteMapping("/{resourceKey}")
    public ResponseEntity<Void> release(
            @PathVariable String resourceKey,
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User currentUser
    ) {
        blockService.release(resourceKey, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }
}
