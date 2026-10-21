package org.seitmolabs.modules.booking.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.seitmolabs.common.exceptions.ConflictException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatOrderLockService {

    private static final String LOCK_PREFIX = "booking:seat:";

    private final RedissonClient redissonClient;

    public <T> T execute(Long screeningId, List<Long> seatIds, Supplier<T> operation) {
        List<RLock> acquired = new ArrayList<>();
        try {
            for (Long seatId : seatIds.stream().sorted().toList()) {
                RLock lock = redissonClient.getLock(LOCK_PREFIX + screeningId + ":" + seatId);
                if (!lock.tryLock(2, TimeUnit.SECONDS)) {
                    throw new ConflictException("Выбранные места сейчас оформляет другой клиент");
                }
                acquired.add(lock);
            }
            return operation.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ConflictException("Оформление было прервано во время ожидания мест");
        } finally {
            for (int index = acquired.size() - 1; index >= 0; index--) {
                RLock lock = acquired.get(index);
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}
