package org.seitmolabs.modules.booking.hold.repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.seitmolabs.modules.booking.hold.domain.SeatHold;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SeatHoldRepository {

    private static final String HOLD_PREFIX = "hold:";
    private static final String SEAT_HOLD_PREFIX = "seat-hold:";

    private static final DefaultRedisScript<Long> CREATE_SCRIPT = new DefaultRedisScript<>("""
            for index = 2, #KEYS do
                if redis.call('EXISTS', KEYS[index]) == 1 then
                    return 0
                end
            end
            redis.call('PSETEX', KEYS[1], ARGV[1], ARGV[2])
            for index = 2, #KEYS do
                redis.call('PSETEX', KEYS[index], ARGV[1], ARGV[3])
            end
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return 0
            end
            for index = 2, #KEYS do
                if redis.call('GET', KEYS[index]) == ARGV[1] then
                    redis.call('DEL', KEYS[index])
                end
            end
            redis.call('DEL', KEYS[1])
            return 1
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public boolean create(SeatHold hold, Duration ttl) {
        List<String> keys = keys(hold);
        Long result = redisTemplate.execute(
                CREATE_SCRIPT,
                keys,
                Long.toString(ttl.toMillis()),
                serialize(hold),
                hold.id()
        );
        return Long.valueOf(1).equals(result);
    }

    public Optional<SeatHold> findById(String holdId) {
        String json = redisTemplate.opsForValue().get(holdKey(holdId));
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, SeatHold.class));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Не удалось прочитать удержание мест", exception);
        }
    }

    public boolean isHeld(Long screeningId, Long seatId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(seatKey(screeningId, seatId)));
    }

    public void release(SeatHold hold) {
        redisTemplate.execute(RELEASE_SCRIPT, keys(hold), hold.id());
    }

    private List<String> keys(SeatHold hold) {
        List<String> keys = new ArrayList<>(hold.seatIds().size() + 1);
        keys.add(holdKey(hold.id()));
        hold.seatIds().stream()
                .map(seatId -> seatKey(hold.screeningId(), seatId))
                .forEach(keys::add);
        return keys;
    }

    private String serialize(SeatHold hold) {
        try {
            return objectMapper.writeValueAsString(hold);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Не удалось сохранить удержание мест", exception);
        }
    }

    private String holdKey(String holdId) {
        return HOLD_PREFIX + holdId;
    }

    private String seatKey(Long screeningId, Long seatId) {
        return SEAT_HOLD_PREFIX + screeningId + ":" + seatId;
    }
}
