package org.seitmolabs.modules.notification.repositories;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;
import java.util.function.Supplier;

import com.mongodb.ReadPreference;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.extern.slf4j.Slf4j;
import org.seitmolabs.modules.notification.dto.NotificationSearchPage;
import org.seitmolabs.modules.notification.dto.NotificationSearchRequest;
import org.seitmolabs.modules.notification.dto.NotificationSearchResult;
import org.seitmolabs.modules.notification.dto.NotificationSearchResult.CacheStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Repository
@Slf4j
public class NotificationSearchCache {

    static final String VERSIONS = "notification_search_versions";
    private static final String KEY_PREFIX = "notification:search:v1:";

    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;
    private final Duration ttl;
    // A new process may have run startup migrations before serving requests.
    private final String instanceId = UUID.randomUUID().toString();

    public NotificationSearchCache(MongoTemplate mongoTemplate, StringRedisTemplate redis, JsonMapper jsonMapper,
            @Value("${app.notification.search-cache-ttl-seconds:60}") long ttlSeconds) {
        if (ttlSeconds < 1) {
            throw new IllegalArgumentException("app.notification.search-cache-ttl-seconds must be positive");
        }
        this.mongoTemplate = mongoTemplate;
        this.redis = redis;
        this.jsonMapper = jsonMapper;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    public NotificationSearchResult getOrLoad(String userId, NotificationSearchRequest request,
            Supplier<NotificationSearchPage> load) {
        String version = mongoTemplate.execute(VERSIONS, collection -> {
            var stored = collection.withReadPreference(ReadPreference.primary()).find(Filters.eq("_id", userId)).first();
            return stored == null ? "initial" : stored.getString("version");
        });

        String key;
        try {
            key = KEY_PREFIX + digest(jsonMapper.writeValueAsBytes(new Key(instanceId, userId, version, request)));
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                return new NotificationSearchResult(jsonMapper.readValue(cached, NotificationSearchPage.class),
                        CacheStatus.HIT);
            }
        } catch (DataAccessException | JacksonException exception) {
            log.debug("Notification search cache read failed", exception);
            return new NotificationSearchResult(load.get(), CacheStatus.BYPASS);
        }

        NotificationSearchPage page = load.get();
        try {
            // A concurrent mutation can only make this key obsolete, never current again.
            redis.opsForValue().set(key, jsonMapper.writeValueAsString(page), ttl);
            return new NotificationSearchResult(page, CacheStatus.MISS);
        } catch (DataAccessException | JacksonException exception) {
            log.debug("Notification search cache write failed", exception);
            return new NotificationSearchResult(page, CacheStatus.BYPASS);
        }
    }

    public void invalidate(String userId) {
        // Same MongoDB transaction as the notification write. Redis may be offline during a mutation.
        try {
            mongoTemplate.execute(VERSIONS, collection -> collection.updateOne(Filters.eq("_id", userId),
                    Updates.set("version", UUID.randomUUID().toString()), new UpdateOptions().upsert(true)));
        } catch (DuplicateKeyException exception) {
            // Two first writes for the same owner can both try to insert the version document.
            throw new OptimisticLockingFailureException("Concurrent notification cache version creation", exception);
        }
    }

    private record Key(String instanceId, String userId, String version, NotificationSearchRequest request) {
    }

    private static String digest(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
