package org.seitmolabs.modules.booking.hold.repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;

import org.seitmolabs.modules.booking.hold.domain.SeatHold;
import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEventType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Repository
@RequiredArgsConstructor
public class SeatHoldRepository {

    private static final String HOLD_PREFIX = "hold:";
    private static final String SEAT_HOLD_PREFIX = "seat-hold:";

    private static final DefaultRedisScript<Long> CREATE_SCRIPT =
            new DefaultRedisScript<>("""
            local lastHoldKey = #KEYS - 1
            local streamKey = KEYS[#KEYS]

            -- Проверки выполняются до первой записи.
            local expiresAt = tonumber(ARGV[1])
            local time = redis.call('TIME')
            local now = tonumber(time[1]) * 1000
                + math.floor(tonumber(time[2]) / 1000)

            if not expiresAt or expiresAt <= now then
                return redis.error_reply('Hold expiration must be in the future')
            end

            local streamType = redis.call('TYPE', streamKey).ok

            if streamType ~= 'none' and streamType ~= 'stream' then
                return redis.error_reply('Outbox key must be a stream')
            end

            if streamType == 'stream'
                and redis.call('PTTL', streamKey) ~= -1 then
                return redis.error_reply('Outbox stream must not expire')
            end

            for index = 1, lastHoldKey do
                if redis.call('EXISTS', KEYS[index]) == 1 then
                    return 0
                end
            end

            -- Все эти ключи отсутствовали перед началом записи.
            -- Другие команды не могут вмешаться во время скрипта.
            local function rollbackHold()
                for index = 1, lastHoldKey do
                    redis.call('DEL', KEYS[index])
                end
            end

            for index = 1, lastHoldKey do
                local value = ARGV[3]

                if index == 1 then
                    value = ARGV[2]
                end

                local result = redis.pcall(
                    'SET', KEYS[index], value, 'PXAT', ARGV[1]
                )

                if type(result) == 'table' and result.err then
                    rollbackHold()
                    return redis.error_reply(result.err)
                end
            end

            -- Последняя изменяющая команда: после неё откат не требуется.
            local eventResult = redis.pcall(
                'XADD', streamKey, '*', 'payload', ARGV[4]
            )

            if type(eventResult) == 'table' and eventResult.err then
                rollbackHold()
                return redis.error_reply(eventResult.err)
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
    private final JsonMapper jsonMapper;

    public boolean createWithEvent(SeatHold hold, OutboxEvent event) {

        if (hold.seatIds().isEmpty()) {
            throw new IllegalArgumentException("Список мест не должен быть пустым");
        }

        if (event.getEventType() != OutboxEventType.SEAT_HOLD_CREATED
                || !hold.id().equals(event.getAggregateId())) {
            throw new IllegalArgumentException(
                    "Событие должно относиться к создаваемому удержанию"
            );
        }

        String holdJson = serialize(hold);
        String eventJson = serialize(event);

        List<String> scriptKeys = new ArrayList<>(keys(hold));
        scriptKeys.add(HoldOutboxRepository.STREAM_KEY);

        Long result = redisTemplate.execute(
                CREATE_SCRIPT,
                scriptKeys,
                Long.toString(hold.expiresAt().toEpochMilli()),
                holdJson,
                hold.id(),
                eventJson
        );

        if (result == null) {
            throw new IllegalStateException(
                    "Redis не вернул результат создания удержания"
            );
        }

        return result == 1L;
    }

    public record MeasuredCreation(boolean created, Long replicasAcked, long waitDurationMs, String waitStatus) {}

    /** EVAL and WAIT use the same connection: WAIT tracks writes made by that connection. */
    /* Тут мы дожидаемся ответа от реплик, если он не приходит, то кидается exception */
    public MeasuredCreation createWithReplicationCheck(SeatHold hold, OutboxEvent event, boolean wait) {

        if (hold.seatIds().isEmpty() || event.getEventType() != OutboxEventType.SEAT_HOLD_CREATED
                || !hold.id().equals(event.getAggregateId())) {
            throw new IllegalArgumentException("Событие должно относиться к удержанию с местами");
        }

        List<String> scriptKeys = new ArrayList<>(keys(hold));
        scriptKeys.add(HoldOutboxRepository.STREAM_KEY);
        List<String> arguments = new ArrayList<>(scriptKeys);

        arguments.add(Long.toString(hold.expiresAt().toEpochMilli()));
        arguments.add(serialize(hold));
        arguments.add(hold.id());
        arguments.add(serialize(event));

        byte[][] encoded = arguments.stream().map(value -> value.getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);

        MeasuredCreation result = redisTemplate.execute((RedisCallback<MeasuredCreation>) connection -> {

            Long created = connection.scriptingCommands().eval(
                    CREATE_SCRIPT.getScriptAsString().getBytes(StandardCharsets.UTF_8),
                    ReturnType.INTEGER, scriptKeys.size(), encoded);

            if (created == null) throw new IllegalStateException("Redis не вернул результат создания удержания");
            if (created != 1L) return new MeasuredCreation(false, null, 0, "NOT_REQUESTED");
            if (!wait) return new MeasuredCreation(true, null, 0, "NOT_REQUESTED");

            long started = System.nanoTime();

            try {

                @SuppressWarnings("unchecked")
                RedisClusterAsyncCommands<byte[], byte[]> commands =
                        (RedisClusterAsyncCommands<byte[], byte[]>) connection.getNativeConnection();

                Long reply = commands.waitForReplication(1, 500).get(2, TimeUnit.SECONDS);
                long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
                if (reply == null) return new MeasuredCreation(true, null, duration, "UNKNOWN");
                long acked = reply;

                return new MeasuredCreation(true, acked, duration, acked >= 1 ? "ACKNOWLEDGED" : "NOT_ACKNOWLEDGED");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return new MeasuredCreation(true, null,
                        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started), "UNKNOWN");
            } catch (ExecutionException | TimeoutException | RuntimeException exception) {
                // The hold is already created; a failed WAIT must not cause a duplicate attempt.
                return new MeasuredCreation(true, null,
                        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started), "UNKNOWN");
            }
        });
        if (result == null) throw new IllegalStateException("Redis не вернул результат создания удержания");
        return result;
    }

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
            return Optional.of(jsonMapper.readValue(json, SeatHold.class));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Не удалось прочитать удержание мест", exception);
        }
    }

    public boolean isHeld(Long screeningId, Long seatId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(seatKey(screeningId, seatId)));
    }

    public void release(SeatHold hold) {
        redisTemplate.execute(RELEASE_SCRIPT, keys(hold), hold.id());
    }

    public List<String> keys(SeatHold hold) {
        List<String> keys = new ArrayList<>(hold.seatIds().size() + 1);
        keys.add(holdKey(hold.id()));
        hold.seatIds().stream()
                .map(seatId -> seatKey(hold.screeningId(), seatId))
                .forEach(keys::add);
        return keys;
    }

    private String serialize(Object value) {
        try {
            return jsonMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Не удалось сериализовать удержание или событие",
                    exception
            );
        }
    }

    private String holdKey(String holdId) {
        return HOLD_PREFIX + holdId;
    }

    private String seatKey(Long screeningId, Long seatId) {
        return SEAT_HOLD_PREFIX + screeningId + ":" + seatId;
    }
}
