package org.seitmolabs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seitmolabs.modules.booking.hold.domain.SeatHold;
import org.seitmolabs.modules.booking.hold.repository.SeatHoldRepository;
import org.seitmolabs.modules.booking.hold.repository.HoldOutboxRepository;
import org.seitmolabs.modules.booking.hold.service.HoldOutboxProcessor;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.repositories.CategoryCacheRepository;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.seitmolabs.modules.consistency.topology.RedisReplicaReader;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.seitmolabs.modules.notification.repositories.NotificationRepository;
import org.seitmolabs.modules.notification.factory.NotificationFactory;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.seitmolabs.modules.notification.outbox.factory.OutboxEventFactory;
import org.seitmolabs.modules.notification.service.NotificationServiceImpl;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.seitmolabs.modules.consistency.topology.RedisNodeRegistry;

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
    private static RedisReplicaReader reader;

    @BeforeAll
    static void connect() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();

        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        jsonMapper = JsonMapper.builder().findAndAddModules().build();

        RedisNodeRegistry registry = mock(RedisNodeRegistry.class);
        when(registry.replicas()).thenReturn(List.of());

        reader = new RedisReplicaReader(registry, redisTemplate);
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

        NotificationRepository repository = new NotificationRepository(reader, redisTemplate, jsonMapper);


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
    void createsHoldAllSeatKeysAndMatchingEventTogether() {
        var holds = new SeatHoldRepository(redisTemplate, jsonMapper);
        var outbox = new HoldOutboxRepository(redisTemplate, jsonMapper);
        SeatHold hold = hold("hold-1", List.of(10L, 11L));
        OutboxEvent event = event(hold);

        assertThat(holds.createWithEvent(hold, event)).isTrue();

        assertThat(holds.findById(hold.id())).contains(hold);
        assertThat(holds.isHeld(5L, 10L)).isTrue();
        assertThat(holds.isHeld(5L, 11L)).isTrue();
        for (String key : holds.keys(hold)) {
            assertThat(redisTemplate.getExpire(key, TimeUnit.MILLISECONDS))
                    .isPositive().isLessThanOrEqualTo(60_000L);
        }
        var records = outbox.findAfter("0-0");
        assertThat(records).hasSize(1);
        assertThat(outbox.deserialize(records.getFirst()))
                .usingRecursiveComparison().isEqualTo(event);
        assertThat(redisTemplate.getExpire(HoldOutboxRepository.STREAM_KEY))
                .isEqualTo(-1L);
    }

    @Test
    void conflictingHoldCreatesNeitherPartialSeatKeysNorEvent() {
        var holds = new SeatHoldRepository(redisTemplate, jsonMapper);
        var outbox = new HoldOutboxRepository(redisTemplate, jsonMapper);
        SeatHold first = hold("hold-1", List.of(10L, 11L));
        SeatHold conflicting = hold("hold-2", List.of(11L, 12L));
        OutboxEvent firstEvent = event(first);

        assertThat(holds.createWithEvent(first, firstEvent)).isTrue();
        assertThat(holds.createWithEvent(conflicting, event(conflicting))).isFalse();

        assertThat(holds.findById(first.id())).contains(first);
        assertThat(holds.findById(conflicting.id())).isEmpty();
        assertThat(holds.isHeld(5L, 12L)).isFalse();
        assertThat(redisTemplate.opsForValue().get("seat-hold:5:11"))
                .isEqualTo(first.id());
        var records = outbox.findAfter("0-0");
        assertThat(records).hasSize(1);
        assertThat(outbox.deserialize(records.getFirst()).getId())
                .isEqualTo(firstEvent.getId());
    }

    @Test
    void holdExpiresButUndeliveredEventRemains() throws InterruptedException {
        var holds = new SeatHoldRepository(redisTemplate, jsonMapper);
        var outbox = new HoldOutboxRepository(redisTemplate, jsonMapper);
        Instant now = Instant.now();
        SeatHold hold = new SeatHold("hold-1", 1L, 5L, List.of(10L, 11L),
                now, now.plusSeconds(2));
        OutboxEvent event = event(hold);

        assertThat(holds.createWithEvent(hold, event)).isTrue();

        // Bounded polling checks actual expiry without assuming exact Redis timing.
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (redisTemplate.countExistingKeys(holds.keys(hold)) > 0
                && System.nanoTime() < deadline) {
            Thread.sleep(25);
        }

        assertThat(redisTemplate.countExistingKeys(holds.keys(hold))).isZero();
        var records = outbox.findAfter("0-0");
        assertThat(records).hasSize(1);
        assertThat(outbox.deserialize(records.getFirst()).getId()).isEqualTo(event.getId());
    }

    @Test
    void invalidOutboxKeyDoesNotCreateHoldOrOverwriteExistingData() {
        var holds = new SeatHoldRepository(redisTemplate, jsonMapper);
        SeatHold hold = hold("hold-1", List.of(10L, 11L));
        redisTemplate.opsForValue().set(HoldOutboxRepository.STREAM_KEY, "wrong-type");

        assertThatThrownBy(() -> holds.createWithEvent(hold, event(hold)))
                .isInstanceOf(RedisSystemException.class)
                .hasStackTraceContaining("Outbox key must be a stream");

        assertThat(redisTemplate.countExistingKeys(holds.keys(hold))).isZero();
        assertThat(redisTemplate.opsForValue().get(HoldOutboxRepository.STREAM_KEY))
                .isEqualTo("wrong-type");
    }

    @Test
    void eventAppendFailureRollsBackAllCreatedHoldKeys() {
        var holds = new SeatHoldRepository(redisTemplate, jsonMapper);
        var outbox = new HoldOutboxRepository(redisTemplate, jsonMapper);
        SeatHold hold = hold("hold-1", List.of(10L, 11L));
        String maxId = "18446744073709551615-18446744073709551615";
        var existing = MapRecord.create(HoldOutboxRepository.STREAM_KEY,
                Map.of("payload", "existing-event")).withId(RecordId.of(maxId));
        redisTemplate.<String, String>opsForStream().add(existing);

        // The stream has the correct type, but XADD * cannot allocate another ID.
        // This fails after SET commands and exercises the Lua compensation branch.
        assertThatThrownBy(() -> holds.createWithEvent(hold, event(hold)))
                .isInstanceOf(RedisSystemException.class)
                .hasStackTraceContaining("exhausted");

        assertThat(redisTemplate.countExistingKeys(holds.keys(hold))).isZero();
        var remaining = outbox.findAfter("0-0");
        assertThat(remaining).hasSize(1);
        assertThat(remaining.getFirst().getId().getValue()).isEqualTo(maxId);
        assertThat(remaining.getFirst().getValue()).containsEntry("payload", "existing-event");
    }

    @Test
    void concurrentOverlappingHoldsProduceOnlyOneHoldAndItsEvent() throws Exception {
        var holds = new SeatHoldRepository(redisTemplate, jsonMapper);
        var outbox = new HoldOutboxRepository(redisTemplate, jsonMapper);
        SeatHold first = hold("hold-1", List.of(10L, 11L));
        SeatHold second = hold("hold-2", List.of(11L, 12L));
        var start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var firstResult = executor.submit(() -> {
                if (!start.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Concurrent test did not start");
                }
                return holds.createWithEvent(first, event(first));
            });
            var secondResult = executor.submit(() -> {
                if (!start.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Concurrent test did not start");
                }
                return holds.createWithEvent(second, event(second));
            });
            start.countDown();

            boolean firstCreated = firstResult.get(10, TimeUnit.SECONDS);
            boolean secondCreated = secondResult.get(10, TimeUnit.SECONDS);
            assertThat(firstCreated).isNotEqualTo(secondCreated);

            SeatHold winner = firstCreated ? first : second;
            SeatHold loser = firstCreated ? second : first;
            assertThat(holds.findById(winner.id())).contains(winner);
            assertThat(holds.findById(loser.id())).isEmpty();
            for (Long seatId : winner.seatIds()) {
                assertThat(redisTemplate.opsForValue().get("seat-hold:5:" + seatId))
                        .isEqualTo(winner.id());
            }
            for (Long seatId : loser.seatIds()) {
                if (!winner.seatIds().contains(seatId)) {
                    assertThat(holds.isHeld(5L, seatId)).isFalse();
                }
            }
            var records = outbox.findAfter("0-0");
            assertThat(records).hasSize(1);
            assertThat(outbox.deserialize(records.getFirst()).getAggregateId())
                    .isEqualTo(winner.id());
        }
    }

    @Test
    void failedDeliveryRemainsPendingAndIsRetriedSuccessfully() {
        var holds = new SeatHoldRepository(redisTemplate, jsonMapper);
        var outbox = new HoldOutboxRepository(redisTemplate, jsonMapper);

        var notifications = new NotificationRepository(reader, redisTemplate, jsonMapper);

        var categories = mock(CategoryDbRepository.class);
        when(categories.findBySystemCode(Category.BOOKING_CODE))
                .thenReturn(Optional.empty(), Optional.of(bookingCategory()));
        var processor = new HoldOutboxProcessor(outbox,
                notificationService(notifications, categories));
        SeatHold hold = hold("hold-1", List.of(10L));
        assertThat(holds.createWithEvent(hold, event(hold))).isTrue();

        processor.deliverPending();

        assertThat(outbox.findAfter("0-0")).hasSize(1);
        assertThat(notifications.findIdsByUserId("client01")).isEmpty();

        // Finish the first scan, then revisit the failed entry on the next scan.
        processor.deliverPending();
        processor.deliverPending();

        assertThat(outbox.findAfter("0-0")).isEmpty();
        assertThat(notifications.findIdsByUserId("client01")).hasSize(1);
        assertThat(holds.findById(hold.id())).contains(hold);
    }

    @Test
    void retryAfterNotificationWasSavedDoesNotCreateDuplicate() {
        var holds = new SeatHoldRepository(redisTemplate, jsonMapper);
        var outbox = new HoldOutboxRepository(redisTemplate, jsonMapper);
        var failingOutbox = spy(outbox);
        var notifications = new NotificationRepository(reader, redisTemplate, jsonMapper);
        var categories = mock(CategoryDbRepository.class);
        when(categories.findBySystemCode(Category.BOOKING_CODE))
                .thenReturn(Optional.of(bookingCategory()));
        var service = notificationService(notifications, categories);
        SeatHold hold = hold("hold-1", List.of(10L));
        OutboxEvent event = event(hold);
        assertThat(holds.createWithEvent(hold, event)).isTrue();

        // Reproduce the failure window: notification exists, queue deletion fails.
        doThrow(new IllegalStateException("Simulated queue deletion failure"))
                .when(failingOutbox).deleteDelivered(any(RecordId.class));
        new HoldOutboxProcessor(failingOutbox, service).deliverPending();

        var originalIds = notifications.findIdsByUserId("client01");
        assertThat(originalIds).hasSize(1);
        assertThat(outbox.findAfter("0-0")).hasSize(1);

        // A new processor has no in-memory cursor, as after a backend restart.
        new HoldOutboxProcessor(outbox, service).deliverPending();

        assertThat(outbox.findAfter("0-0")).isEmpty();
        assertThat(notifications.findIdsByUserId("client01")).isEqualTo(originalIds);
        assertThat(notifications.findById(originalIds.getFirst()).orElseThrow()
                .getSourceEventId()).isEqualTo(event.getId().toString());
    }

    @Test
    void experimentalWaitWithoutReplicasKeepsHoldAndEvent() {
        var holds = new SeatHoldRepository(redisTemplate, jsonMapper);
        var outbox = new HoldOutboxRepository(redisTemplate, jsonMapper);
        SeatHold hold = hold("hold-wait", List.of(10L));

        var result = holds.createWithReplicationCheck(hold, event(hold), true);

        assertThat(result.created()).isTrue();
        assertThat(result.waitStatus()).isEqualTo("NOT_ACKNOWLEDGED");
        assertThat(result.replicasAcked()).isZero();
        assertThat(holds.findById(hold.id())).contains(hold);
        assertThat(outbox.findAfter("0-0")).hasSize(1);

        SeatHold conflicting = hold("hold-conflict", List.of(10L));
        assertThat(holds.createWithReplicationCheck(conflicting, event(conflicting), true).created()).isFalse();
        assertThat(outbox.findAfter("0-0")).hasSize(1);
    }

    @Test
    void cachesCategoryAndCategoryList() {
        CategoryCacheRepository repository = new CategoryCacheRepository(redisTemplate, jsonMapper, reader);
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

    private SeatHold hold(String id, List<Long> seatIds) {
        Instant now = Instant.now();
        return new SeatHold(id, 1L, 5L, seatIds, now, now.plusSeconds(60));
    }

    private OutboxEvent event(SeatHold hold) {
        return new OutboxEventFactory().holdCreated(hold, "client01");
    }

    private Category bookingCategory() {
        return new Category(1L, "Бронирование", "Изменения состояния билета",
                Category.BOOKING_CODE);
    }

    private NotificationServiceImpl notificationService(
            NotificationRepository notifications, CategoryDbRepository categories) {
        return new NotificationServiceImpl(notifications, mock(UserRepository.class),
                new NotificationFactory(), categories);
    }

}
