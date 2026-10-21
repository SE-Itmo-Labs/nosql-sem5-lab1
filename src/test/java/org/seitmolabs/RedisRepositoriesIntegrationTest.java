package org.seitmolabs;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seitmolabs.modules.booking.hold.domain.SeatHold;
import org.seitmolabs.modules.booking.hold.repository.SeatHoldRepository;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.repositories.CategoryCacheRepository;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.seitmolabs.modules.notification.repositories.NotificationRepository;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import tools.jackson.databind.json.JsonMapper;

@Testcontainers
class RedisRepositoriesIntegrationTest {

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(
            DockerImageName.parse("redis:8.10.1-alpine")
    ).withExposedPorts(6379);

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redisTemplate;
    private static JsonMapper jsonMapper;

    @BeforeAll
    static void connect() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();

        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        jsonMapper = JsonMapper.builder().findAndAddModules().build();

    }

    @AfterAll
    static void disconnect() {
        connectionFactory.destroy();
    }

    @BeforeEach
    void clearRedis() {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        try {
            connection.serverCommands().flushDb();
        } finally {
            connection.close();
        }
    }

    @Test
    void storesNotificationAndUserIndexTogether() {
        NotificationRepository repository = new NotificationRepository(redisTemplate, jsonMapper);
        Long id = repository.nextId();
        Notification notification = new Notification(
                id,
                "client01",
                "Начало сеанса",
                "Фильм начнётся через 15 минут",
                1L,
                NotificationStatus.SENT,
                Instant.parse("2026-10-08T10:00:00Z")
        );

        repository.save(notification);

        assertThat(repository.findById(id)).contains(notification);
        assertThat(repository.findIdsByUserId("client01")).containsExactly(id);
    }

    @Test
    void seatHoldIsAtomicAndExpires() throws InterruptedException {
        SeatHoldRepository repository = new SeatHoldRepository(redisTemplate, jsonMapper);
        SeatHold first = new SeatHold("hold-1", 1L, 5L, List.of(10L, 11L),
                Instant.now(), Instant.now().plusMillis(250));
        SeatHold conflicting = new SeatHold("hold-2", 2L, 5L, List.of(11L, 12L),
                Instant.now(), Instant.now().plusSeconds(1));

        assertThat(repository.create(first, Duration.ofMillis(250))).isTrue();
        assertThat(repository.create(conflicting, Duration.ofSeconds(1))).isFalse();
        assertThat(repository.isHeld(5L, 12L)).isFalse();
        Thread.sleep(350);
        assertThat(repository.findById(first.id())).isEmpty();
    }

    @Test
    void cachesCategoryAndCategoryList() {
        CategoryCacheRepository repository = new CategoryCacheRepository(redisTemplate, jsonMapper);
        Category category = new Category(1L, "Бронирование", "Изменения состояния билета");

        repository.put(category, Duration.ofMinutes(5));
        repository.putAll(List.of(category), Duration.ofMinutes(5));

        assertThat(repository.findById(1L)).contains(category);
        assertThat(repository.findAll()).contains(List.of(category));
        assertThat(repository.getRemainingTtlSeconds(1L)).isPositive();

        repository.delete(1L);
        repository.deleteList();

        assertThat(repository.findById(1L)).isEmpty();
        assertThat(repository.findAll()).isEmpty();
    }

}
