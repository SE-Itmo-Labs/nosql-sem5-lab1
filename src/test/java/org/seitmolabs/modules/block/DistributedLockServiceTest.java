package org.seitmolabs.modules.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

class DistributedLockServiceTest {

    private RLock lock;
    private DistributedLockService service;

    @BeforeEach
    void setUp() {
        RedissonClient redissonClient = mock(RedissonClient.class);
        lock = mock(RLock.class);
        when(redissonClient.getLock("lock:notification:1")).thenReturn(lock);
        service = new DistributedLockService(redissonClient);
    }

    @Test
    void executesCodeWhenLockIsAcquired() {
        AtomicBoolean executed = new AtomicBoolean(false);
        when(lock.tryLock()).thenReturn(true);

        boolean result = service.executeWithLock(
                "notification:1",
                () -> executed.set(true)
        );

        assertThat(result).isTrue();
        assertThat(executed).isTrue();
        verify(lock).unlock();
    }

    @Test
    void skipsCodeWhenLockIsBusy() {
        Runnable criticalSection = mock(Runnable.class);
        when(lock.tryLock()).thenReturn(false);

        boolean result = service.executeWithLock("notification:1", criticalSection);

        assertThat(result).isFalse();
        verify(criticalSection, never()).run();
        verify(lock, never()).unlock();
    }

    @Test
    void releasesLockWhenCodeFails() {
        when(lock.tryLock()).thenReturn(true);
        Runnable criticalSection = () -> {
            throw new IllegalStateException("Ошибка операции");
        };

        assertThatThrownBy(() -> service.executeWithLock("notification:1", criticalSection))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Ошибка операции");

        verify(lock).unlock();
    }
}
