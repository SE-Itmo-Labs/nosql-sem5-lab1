package org.seitmolabs.modules.booking.hold.repository;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HoldOutboxRepository {

    public static final String STREAM_KEY = "outbox:seat-holds";

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;

    public List<MapRecord<String, String, String>> findAfter(String recordId) {

        var range = Range.rightUnbounded(
                Range.Bound.exclusive(recordId)
        );

        var records = redisTemplate.<String, String>opsForStream().range(STREAM_KEY, range, Limit.limit().count(20));

        return records == null ? List.of() : records;
    }

    public OutboxEvent deserialize(MapRecord<String, String, String> record) {
        String payload = record.getValue().get("payload");

        if (payload == null) {
            throw new IllegalStateException(
                    "Нет payload у события " + record.getId()
            );
        }

        return jsonMapper.readValue(payload, OutboxEvent.class);
    }

    public void deleteDelivered(RecordId recordId) {
        redisTemplate.opsForStream().delete(STREAM_KEY, recordId);
    }
}
