package org.seitmolabs.service;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Block;
import org.seitmolabs.repository.BlockRepository;
import org.springframework.stereotype.Service;

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
}