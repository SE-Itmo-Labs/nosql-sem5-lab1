package org.seitmolabs.modules.notification.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.seitmolabs.common.exceptions.GlobalExceptionHandler;
import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.seitmolabs.modules.notification.controller.AdminNotificationController;
import org.seitmolabs.modules.notification.controller.NotificationController;
import org.seitmolabs.modules.notification.domain.ProcessingRule;
import org.seitmolabs.modules.notification.factory.NotificationFactory;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEventType;
import org.seitmolabs.modules.notification.service.NotificationServiceImpl;
import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
class NotificationMongoIntegrationTest {

    private static final String DATABASE = "notifications_test";
    private static final String CURRENT_USER = SimpleAuthFilter.CURRENT_USER_ATTRIBUTE;

    @Container
    private static final GenericContainer<?> MONGO = new GenericContainer<>(DockerImageName.parse("mongo:8.0"))
            .withExposedPorts(27017);

    @Test
    void preservesNotificationApiAndDeduplicatesDeliveryAcrossRestarts() throws Exception {
        String uri = "mongodb://%s:%d".formatted(MONGO.getHost(), MONGO.getMappedPort(27017));
        User recipient = new User();
        recipient.setUsername("client01");
        User admin = new User();
        admin.setUsername("admin");
        OutboxEvent event = OutboxEvent.builder().id(UUID.randomUUID()).username("client01")
                .eventType(OutboxEventType.ORDER_CONFIRMED)
                .title("Заказ подтверждён").message("Заказ №1").build();

        try (MongoClient client = MongoClients.create(uri)) {
            var notifications = client.getDatabase(DATABASE).getCollection("notifications");
            notifications.insertOne(new Document("_id", 41L).append("userId", "other-user"));

            try (var context = openContext(client, DATABASE)) {
                var service = notificationService(context, recipient);
                MockMvcBuilders.standaloneSetup(new AdminNotificationController(service)).build()
                        .perform(post("/api/v1/admin/notifications").contentType(MediaType.APPLICATION_JSON)
                                .requestAttr(CURRENT_USER, admin)
                                .content("""
                                        {"userId":"client01","title":"Заголовок","text":"Текст","categoryId":1}
                                        """))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").value(42))
                        .andExpect(jsonPath("$.status").value("SENT"))
                        .andExpect(jsonPath("$.updatedAt").doesNotExist())
                        .andExpect(jsonPath("$.actionLog").doesNotExist());
                service.deliver(event);
            }

            try (var context = openContext(client, DATABASE)) {
                var service = notificationService(context, recipient);
                service.deliver(event);
                var adminApi = MockMvcBuilders.standaloneSetup(new AdminNotificationController(service))
                        .setControllerAdvice(new GlobalExceptionHandler()).build();
                var api = MockMvcBuilders.standaloneSetup(new NotificationController(service)).build();
                context.getBean(MongoTemplate.class).insert(new ProcessingRule(7L, "Подтверждение бронирования",
                        OutboxEventType.ORDER_CONFIRMED, List.of("booking", "confirmed")));
                api.perform(get("/api/v1/notifications").requestAttr(CURRENT_USER, recipient))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2));
                api.perform(get("/api/v1/notifications/42").requestAttr(CURRENT_USER, recipient))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("SENT"));
                api.perform(post("/api/v1/notifications/42/read").requestAttr(CURRENT_USER, recipient))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("READ"));
                api.perform(post("/api/v1/notifications/42/read").requestAttr(CURRENT_USER, recipient))
                        .andExpect(status().isOk());
                var manual = notifications.find(new Document("_id", 42L)).first();
                assertThat(manual.getList("actionLog", Document.class)).satisfiesExactly(
                        entry -> assertJournalEntry(entry, "CREATED", "admin"),
                        entry -> assertJournalEntry(entry, "READ", "client01"));
                assertThat(manual.getDate("updatedAt")).isEqualTo(
                        manual.getList("actionLog", Document.class).getLast().getDate("at"));

                adminApi.perform(post("/api/v1/admin/notifications/42/apply-rule").requestAttr(CURRENT_USER, admin)
                                .contentType(MediaType.APPLICATION_JSON).content("{\"ruleId\":7}"))
                        .andExpect(status().isBadRequest());

                var source = new Document("sourceEventId", event.getId().toString());
                long systemNotificationId = notifications.find(source).first().getLong("_id");
                var applyRule = post("/api/v1/admin/notifications/{id}/apply-rule", systemNotificationId)
                        .requestAttr(CURRENT_USER, admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ruleId\":7}");
                adminApi.perform(applyRule).andExpect(status().isOk());
                adminApi.perform(applyRule).andExpect(status().isOk());
                var processed = notifications.find(source).first();
                assertThat(processed.getList("tags", String.class)).containsExactly("booking", "confirmed");
                assertThat(processed.getLong("processingRuleId")).isEqualTo(7L);
                assertThat(processed.getList("actionLog", Document.class)).satisfiesExactly(
                        entry -> assertJournalEntry(entry, "CREATED", "system"),
                        entry -> {
                            assertJournalEntry(entry, "RULE_APPLIED", "admin");
                            assertThat(entry.getLong("processingRuleId")).isEqualTo(7L);
                        });
                assertThat(processed.getDate("updatedAt")).isEqualTo(
                        processed.getList("actionLog", Document.class).getLast().getDate("at"));
                api.perform(delete("/api/v1/notifications/42").requestAttr(CURRENT_USER, recipient))
                        .andExpect(status().isNoContent());
            }

            assertThat(notifications.countDocuments(new Document("_id", 42L))).isZero();
            assertThat(notifications.countDocuments(new Document("sourceEventId", event.getId().toString())))
                    .isEqualTo(1);
        }
    }

    @Test
    void searchesOwnNotificationsWithNestedMatchesAndStablePages() throws Exception {
        String uri = "mongodb://%s:%d".formatted(MONGO.getHost(), MONGO.getMappedPort(27017));
        String database = DATABASE + "_search";
        User recipient = new User();
        recipient.setUsername("reader");
        Date from = Date.from(Instant.parse("2026-10-08T10:00:00Z"));
        Date to = Date.from(Instant.parse("2026-10-09T10:00:00Z"));
        try (MongoClient client = MongoClients.create(uri)) {
            var notifications = client.getDatabase(database).getCollection("notifications");
            notifications.insertMany(List.of(
                    searchFixture(1L, "reader", from),
                    searchFixture(2L, "reader", from).append("updatedAt", from),
                    searchFixture(3L, "other-reader", from),
                    searchFixture(4L, "reader", from).append("updatedAt", to),
                    searchFixture(5L, "reader", from).append("tags", List.of("cancelled"))
            ));

            try (var context = openContext(client, database)) {
                var api = MockMvcBuilders.standaloneSetup(new NotificationController(notificationService(context, recipient)))
                        .setControllerAdvice(new GlobalExceptionHandler()).build();
                for (int page = 0; page < 2; page++) {
                    api.perform(get("/api/v1/notifications/search").requestAttr(CURRENT_USER, recipient)
                                    .param("processingRuleId", "7").param("categoryId", "1").param("status", "READ")
                                    .param("tag", "booking").param("actionType", "RULE_APPLIED").param("actorId", "admin")
                                    .param("updatedFrom", from.toInstant().toString()).param("updatedTo", to.toInstant().toString())
                                    .param("page", Integer.toString(page)).param("size", "1").param("userId", "other-reader"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.items[0].id").value(2 - page))
                            .andExpect(jsonPath("$.items[0].updatedAt").exists())
                            .andExpect(jsonPath("$.items[0].actionLog").doesNotExist())
                            .andExpect(jsonPath("$.page").value(page))
                            .andExpect(jsonPath("$.totalElements").value(2))
                            .andExpect(jsonPath("$.totalPages").value(2));
                }
                api.perform(get("/api/v1/notifications/search").requestAttr(CURRENT_USER, recipient)
                                .param("actionType", "READ").param("actorId", "admin"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items.length()").value(0));
                api.perform(get("/api/v1/notifications/search").requestAttr(CURRENT_USER, recipient)
                                .param("sort", "CREATED_AT_ASC").param("size", "1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items[0].id").value(1));
                api.perform(get("/api/v1/notifications/search").requestAttr(CURRENT_USER, recipient)
                                .param("size", "101"))
                        .andExpect(status().isBadRequest());
                api.perform(get("/api/v1/notifications/search").requestAttr(CURRENT_USER, recipient)
                                .param("updatedFrom", to.toInstant().toString()).param("updatedTo", from.toInstant().toString()))
                        .andExpect(status().isBadRequest());
            }
            assertThat(notifications.find(new Document("_id", 1L)).first().getDate("updatedAt")).isEqualTo(from);
            assertThat(notifications.find(new Document("_id", 4L)).first().getDate("updatedAt")).isEqualTo(to);
        }
    }

    private Document searchFixture(long id, String userId, Date createdAt) {
        return new Document("_id", id)
                .append("userId", userId)
                .append("categoryId", 1L)
                .append("status", "READ")
                .append("processingRuleId", 7L)
                .append("createdAt", createdAt)
                .append("tags", List.of("booking", "confirmed"))
                .append("actionLog", List.of(
                        new Document("type", "RULE_APPLIED").append("actorId", "admin"),
                        new Document("type", "READ").append("actorId", userId)));
    }

    private void assertJournalEntry(Document entry, String type, String actorId) {
        assertThat(entry.getString("type")).isEqualTo(type);
        assertThat(entry.getString("actorId")).isEqualTo(actorId);
        assertThat(entry.getDate("at")).isNotNull();
    }

    private AnnotationConfigApplicationContext openContext(MongoClient client, String database) {
        var context = new AnnotationConfigApplicationContext();
        context.registerBean(MongoDatabaseFactory.class, () -> new SimpleMongoClientDatabaseFactory(client, database));
        context.registerBean(MongoTemplate.class, () -> new MongoTemplate(context.getBean(MongoDatabaseFactory.class)));
        context.register(NotificationMongoConfiguration.class, Repositories.class);
        context.refresh();
        return context;
    }

    private NotificationServiceImpl notificationService(AnnotationConfigApplicationContext context, User recipient) {
        var users = mock(UserRepository.class);
        when(users.findByUsername(recipient.getUsername())).thenReturn(Optional.of(recipient));
        var categories = mock(CategoryDbRepository.class);
        when(categories.findBySystemCode(Category.BOOKING_CODE))
                .thenReturn(Optional.of(new Category(1L, "Бронирование", "Описание", Category.BOOKING_CODE)));
        return new NotificationServiceImpl(context.getBean(MongoNotificationRepository.class), users,
                new NotificationFactory(), categories, context.getBean(MongoTemplate.class));
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableMongoRepositories(basePackageClasses = MongoNotificationRepository.class)
    static class Repositories {
    }
}
