package org.seitmolabs.modules.booking.order.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.UuidRepresentation;
import org.junit.jupiter.api.Test;
import org.seitmolabs.modules.booking.order.domain.CinemaOrder;
import org.seitmolabs.modules.booking.order.domain.OrderProduct;
import org.seitmolabs.modules.booking.order.domain.OrderStatus;
import org.seitmolabs.modules.booking.order.domain.Ticket;
import org.seitmolabs.modules.booking.order.domain.TicketStatus;
import org.seitmolabs.modules.booking.order.repository.CinemaOrderRepository;
import org.seitmolabs.modules.booking.order.repository.OrderProductRepository;
import org.seitmolabs.modules.booking.order.repository.TicketRepository;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEventType;
import org.seitmolabs.modules.notification.outbox.repository.OutboxEventRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
class BookingMongoIntegrationTest {

    @Container
    private static final GenericContainer<?> MONGO = new GenericContainer<>(DockerImageName.parse("mongo:8.0"))
            .withExposedPorts(27017)
            .withCommand("mongod", "--replSet", "rs0", "--bind_ip_all");

    @Test
    void rollsBackBookingWritesAndAllowsResaleAfterCancellation() throws Exception {
        var initialized = MONGO.execInContainer("mongosh", "--quiet", "--eval",
                "rs.initiate({ _id: 'rs0', members: [{ _id: 0, host: 'localhost:27017' }] })");
        assertThat(initialized.getExitCode()).withFailMessage(initialized.getStderr()).isZero();

        String uri = "mongodb://%s:%d/?replicaSet=rs0&directConnection=true"
                .formatted(MONGO.getHost(), MONGO.getMappedPort(27017));
        var settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();

        try (MongoClient client = MongoClients.create(settings);
                var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(MongoDatabaseFactory.class,
                    () -> new SimpleMongoClientDatabaseFactory(client, "booking_test"));
            context.registerBean(MongoTemplate.class,
                    () -> new MongoTemplate(context.getBean(MongoDatabaseFactory.class)));
            context.register(BookingMongoConfiguration.class, Repositories.class);
            context.refresh();
            context.getBean(BookingMongoConfiguration.class).run(null);

            var transactions = context.getBean(TransactionTemplate.class);
            var orders = context.getBean(CinemaOrderRepository.class);
            var tickets = context.getBean(TicketRepository.class);
            var products = context.getBean(OrderProductRepository.class);
            var outbox = context.getBean(OutboxEventRepository.class);

            Ticket confirmed = tickets.insert(ticket(1L));
            UUID eventId = UUID.randomUUID();
            assertThatThrownBy(() -> transactions.executeWithoutResult(status -> {
                CinemaOrder order = orders.insert(CinemaOrder.builder()
                        .userId(1L).holdId("hold-1").idempotencyKey("key-1")
                        .status(OrderStatus.CONFIRMED).createdAt(Instant.now())
                        .totalAmount(new BigDecimal("450.00")).build());
                products.insert(OrderProduct.builder().orderId(order.getId()).productId(1L)
                        .quantity(1).unitPriceSnapshot(BigDecimal.TEN).build());
                outbox.insert(OutboxEvent.builder().id(eventId).createdAt(Instant.now())
                        .eventType(OutboxEventType.ORDER_CONFIRMED).build());
                tickets.insert(ticket(order.getId()));
            })).isInstanceOf(DuplicateKeyException.class);

            assertThat(orders.count()).isZero();
            assertThat(products.count()).isZero();
            assertThat(outbox.findById(eventId)).isEmpty();
            assertThat(tickets.findSeatIds(7L, TicketStatus.CONFIRMED)).containsExactly(2L);
            assertThat(tickets.existsByScreeningIdAndSeatIdInAndStatus(7L, List.of(2L), TicketStatus.CONFIRMED))
                    .isTrue();

            transactions.executeWithoutResult(status -> {
                confirmed.setStatus(TicketStatus.CANCELLED);
                tickets.save(confirmed);
            });
            assertThat(tickets.insert(ticket(2L)).getId()).isNotEqualTo(confirmed.getId());

            outbox.insert(OutboxEvent.builder().id(eventId).createdAt(Instant.now())
                    .eventType(OutboxEventType.ORDER_CONFIRMED).build());
            outbox.recordFailure(eventId, "Redis unavailable", Instant.now());
            outbox.markDelivered(eventId, Instant.now());
            outbox.recordFailure(eventId, "late retry", Instant.now());
            var delivered = outbox.findById(eventId).orElseThrow();
            assertThat(delivered.getDeliveredAt()).isNotNull();
            assertThat(delivered.getAttempts()).isEqualTo(1);
            assertThat(delivered.getLastError()).isNull();
        }
    }

    private Ticket ticket(Long orderId) {
        return Ticket.builder().orderId(orderId).screeningId(7L).seatId(2L)
                .priceSnapshot(new BigDecimal("450.00")).status(TicketStatus.CONFIRMED).build();
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableMongoRepositories(basePackageClasses = {CinemaOrderRepository.class, OutboxEventRepository.class})
    static class Repositories {
    }
}
