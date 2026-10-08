package org.seitmolabs.modules.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seitmolabs.modules.block.dto.BlockResponse;
import org.seitmolabs.modules.block.exceptions.BlockConflictException;

class BlockServiceTest {

    private BlockRepository repository;
    private BlockService service;

    @BeforeEach
    void setUp() {
        repository = mock(BlockRepository.class);
        service = new BlockService(repository);
    }

    @Test
    void createsBlockWithTtl() {
        when(repository.trySave(any(Block.class))).thenReturn(true);
        when(repository.getRemainingTtlSeconds("screening:15:seat:7-12")).thenReturn(300L);

        BlockResponse result = service.create(
                "screening:15:seat:7-12",
                "client01",
                300
        );

        assertThat(result.owner()).isEqualTo("client01");
        assertThat(result.ttlSeconds()).isEqualTo(300);
        assertThat(result.remainingTtlSeconds()).isEqualTo(300);
        assertThat(result.active()).isTrue();
    }

    @Test
    void rejectsSecondBlockForSameResource() {
        when(repository.trySave(any(Block.class))).thenReturn(false);

        assertThatThrownBy(() -> service.create("seat:1", "client01", 60))
                .isInstanceOf(BlockConflictException.class)
                .hasMessage("Ресурс уже заблокирован: seat:1");
    }

    @Test
    void returnsBlockWithRemainingTtl() {
        Block block = block("client01");
        when(repository.findByResourceKey("seat:1")).thenReturn(Optional.of(block));
        when(repository.getRemainingTtlSeconds("seat:1")).thenReturn(42L);

        BlockResponse result = service.get("seat:1");

        assertThat(result.remainingTtlSeconds()).isEqualTo(42);
        assertThat(result.expiresAt()).isEqualTo(block.getCreatedAt().plusSeconds(60));
    }

    @Test
    void ownerCanReleaseBlock() {
        Block block = block("client01");
        when(repository.findByResourceKey("seat:1")).thenReturn(Optional.of(block));
        when(repository.delete(block)).thenReturn(true);

        service.release("seat:1", "client01");

        verify(repository).delete(block);
    }

    @Test
    void anotherUserCannotReleaseBlock() {
        Block block = block("client01");
        when(repository.findByResourceKey("seat:1")).thenReturn(Optional.of(block));

        assertThatThrownBy(() -> service.release("seat:1", "admin"))
                .isInstanceOf(BlockConflictException.class)
                .hasMessage("Освободить блокировку может только её владелец");

        verify(repository, never()).delete(block);
    }

    private Block block(String owner) {
        return new Block(
                "seat:1",
                owner,
                60,
                Instant.parse("2026-10-08T10:00:00Z")
        );
    }
}
