package org.seitmolabs.service;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Block;
import org.seitmolabs.repository.BlockRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * BlockService — сценарий временного хранения: TTL-блокировка ресурса.
 * <p>
 * Назначение: «забронировать» ресурс (место, ресурс обработки) на N секунд.
 * Ключ в Redis получает EX, поэтому освобождение происходит автоматически,
 * без явного удаления.
 */
@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository repository;

    /** Заблокировать ресурс на ttlSeconds. */
    public Block block(String resourceId, String owner, long ttlSeconds) {
        throw new UnsupportedOperationException("TODO: собрать Block + repository.save(block)");
    }

    /** Активна ли блокировка (истечение TTL = блокировка снята). */
    public Optional<Block> getActive(String resourceId) {
        throw new UnsupportedOperationException("TODO: repository.findActive(resourceId)");
    }
}