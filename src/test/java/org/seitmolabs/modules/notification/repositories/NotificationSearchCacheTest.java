package org.seitmolabs.modules.notification.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.seitmolabs.modules.notification.dto.NotificationSearchPage;
import org.seitmolabs.modules.notification.dto.NotificationSearchRequest;
import org.seitmolabs.modules.notification.dto.NotificationSearchResult.CacheStatus;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.json.JsonMapper;

class NotificationSearchCacheTest {

    private final AtomicReference<String> version = new AtomicReference<>("first");
    private final NotificationSearchPage empty = new NotificationSearchPage(List.of(), 0, 20, 0, 0);
    private final NotificationSearchRequest request = requestForPage(0);
    private NotificationSearchCache cache;
    private ValueOperations<String, String> values;

    @BeforeEach
    void setUp() {
        MongoTemplate mongo = mock(MongoTemplate.class);
        when(mongo.execute(eq(NotificationSearchCache.VERSIONS), ArgumentMatchers.<CollectionCallback<String>>any()))
                .thenAnswer(invocation -> version.get());
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> operations = mock(ValueOperations.class);
        values = operations;
        when(redis.opsForValue()).thenReturn(values);
        var entries = new HashMap<String, String>();
        when(values.get(anyString())).thenAnswer(invocation -> entries.get(invocation.getArgument(0)));
        doAnswer(invocation -> {
            assertThat(invocation.<Duration>getArgument(2)).isEqualTo(Duration.ofSeconds(60));
            entries.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(values).set(anyString(), anyString(), any(Duration.class));
        cache = new NotificationSearchCache(mongo, redis, JsonMapper.builder().findAndAddModules().build(), 60);
    }

    @Test
    void ignoresAStaleMissThatFinishesAfterAWrite() {
        var createdAt = Instant.parse("2026-11-20T10:00:00Z");
        var item = new NotificationSearchPage.Item(1L, "alice", "Title", "Text", 1L, NotificationStatus.SENT,
                createdAt, createdAt, null, null, List.of("booking"));
        var latest = new NotificationSearchPage(List.of(item), 0, 20, 1, 1);

        assertThat(cache.getOrLoad("alice", request, () -> {
            version.set("second");
            return empty;
        }).cacheStatus()).isEqualTo(CacheStatus.MISS);
        assertThat(cache.getOrLoad("alice", request, () -> latest).cacheStatus()).isEqualTo(CacheStatus.MISS);
        var hit = cache.getOrLoad("alice", request, () -> empty);
        assertThat(hit.cacheStatus()).isEqualTo(CacheStatus.HIT);
        assertThat(hit.page()).isEqualTo(latest);
        assertThat(cache.getOrLoad("bob", request, () -> empty).cacheStatus()).isEqualTo(CacheStatus.MISS);
        assertThat(cache.getOrLoad("alice", requestForPage(1), () -> empty).cacheStatus())
                .isEqualTo(CacheStatus.MISS);
    }

    @Test
    void bypassesRedisWhenUnavailable() {
        when(values.get(anyString())).thenThrow(new RedisConnectionFailureException("offline"));

        var result = cache.getOrLoad("alice", request, () -> empty);

        assertThat(result.cacheStatus()).isEqualTo(CacheStatus.BYPASS);
        assertThat(result.page()).isEqualTo(empty);
    }

    private NotificationSearchRequest requestForPage(int page) {
        return new NotificationSearchRequest(null, null, null, null, null, null, null, null, null, null, null,
                page, 20, NotificationSearchRequest.Order.UPDATED_AT_DESC);
    }
}
