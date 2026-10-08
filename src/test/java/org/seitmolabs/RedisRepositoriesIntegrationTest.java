package org.seitmolabs;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.seitmolabs.modules.block.Block;
import org.seitmolabs.modules.block.BlockRepository;
import org.seitmolabs.modules.block.DistributedLockService;
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
    private static RedissonClient redissonClient;

    @BeforeAll
    static void connect() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();

        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        jsonMapper = JsonMapper.builder().findAndAddModules().build();

        Config config = new Config();
        config.useSingleServer().setAddress(
                "redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379)
        );
        redissonClient = Redisson.create(config);
    }

    @AfterAll
    static void disconnect() {
        redissonClient.shutdown();
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
    void temporaryBlockExpiresAndCannotBeOverwritten() throws InterruptedException {
        BlockRepository repository = new BlockRepository(redisTemplate, jsonMapper);
        Block block = new Block("seat:7-12", "client01", 1, Instant.now());

        assertThat(repository.trySave(block)).isTrue();
        assertThat(repository.trySave(block)).isFalse();
        assertThat(repository.findByResourceKey("seat:7-12")).contains(block);

        waitUntilBlockExpires(repository, "seat:7-12");

        assertThat(repository.findByResourceKey("seat:7-12")).isEmpty();
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

    @Test
    void distributedLockAllowsOnlyOneExecution() throws Exception {
        DistributedLockService service = new DistributedLockService(redissonClient);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<Boolean> first = executor.submit(() -> service.executeWithLock(
                    "notification:1",
                    () -> holdLock(entered, release)
            ));

            assertThat(entered.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(service.executeWithLock("notification:1", () -> { })).isFalse();

            release.countDown();
            assertThat(first.get(3, TimeUnit.SECONDS)).isTrue();
        }
    }

    private void waitUntilBlockExpires(BlockRepository repository, String resourceKey)
            throws InterruptedException {
        for (int attempt = 0; attempt < 30; attempt++) {
            if (repository.findByResourceKey(resourceKey).isEmpty()) {
                return;
            }
            Thread.sleep(100);
        }
    }

    private void holdLock(CountDownLatch entered, CountDownLatch release) {
        entered.countDown();
        try {
            release.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Ожидание блокировки прервано", e);
        }
    }
}
