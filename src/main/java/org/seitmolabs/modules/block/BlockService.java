package org.seitmolabs.modules.block;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.modules.block.dto.BlockResponse;
import org.seitmolabs.modules.block.exceptions.BlockConflictException;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository repository;

    public Optional<Block> tryBlock(
            String resourceKey,
            String owner,
            long ttlSeconds
    ) {
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException(
                    "TTL блокировки должен быть больше нуля"
            );
        }

        Block block = new Block(
                resourceKey,
                owner,
                ttlSeconds,
                Instant.now()
        );

        boolean saved = repository.trySave(block);

        if (!saved) {
            return Optional.empty();
        }

        return Optional.of(block);
    }

    public BlockResponse create(String resourceKey, String owner, long ttlSeconds) {
        Block block = tryBlock(resourceKey, owner, ttlSeconds)
                .orElseThrow(() -> new BlockConflictException(
                        "Ресурс уже заблокирован: " + resourceKey
                ));

        return toResponse(block);
    }

    public BlockResponse get(String resourceKey) {
        Block block = findBlock(resourceKey);
        return toResponse(block);
    }

    public void release(String resourceKey, String owner) {
        Block block = findBlock(resourceKey);

        if (!block.getOwner().equals(owner)) {
            throw new BlockConflictException("Освободить блокировку может только её владелец");
        }

        if (!repository.delete(block)) {
            throw new EntityNotFoundException("Блокировка уже истекла: " + resourceKey);
        }
    }

    private Block findBlock(String resourceKey) {
        return repository.findByResourceKey(resourceKey)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Блокировка не найдена: " + resourceKey
                ));
    }

    private BlockResponse toResponse(Block block) {
        long remainingTtl = repository.getRemainingTtlSeconds(block.getResourceKey());

        if (remainingTtl == -2) {
            throw new EntityNotFoundException(
                    "Блокировка уже истекла: " + block.getResourceKey()
            );
        }

        return new BlockResponse(
                block.getResourceKey(),
                block.getOwner(),
                block.getTtlSeconds(),
                Math.max(remainingTtl, 0),
                block.getCreatedAt(),
                block.getCreatedAt().plusSeconds(block.getTtlSeconds()),
                true
        );
    }
}
