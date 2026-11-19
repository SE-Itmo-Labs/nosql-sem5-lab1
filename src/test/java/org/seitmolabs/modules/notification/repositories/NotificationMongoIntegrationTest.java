package org.seitmolabs.modules.notification.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

            try (var context = openContext(client)) {
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
                        .andExpect(jsonPath("$.actionLog").doesNotExist());
                service.deliver(event);
            }

            try (var context = openContext(client)) {
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
                api.perform(delete("/api/v1/notifications/42").requestAttr(CURRENT_USER, recipient))
                        .andExpect(status().isNoContent());
            }

            assertThat(notifications.countDocuments(new Document("_id", 42L))).isZero();
            assertThat(notifications.countDocuments(new Document("sourceEventId", event.getId().toString())))
                    .isEqualTo(1);
        }
    }

    private void assertJournalEntry(Document entry, String type, String actorId) {
        assertThat(entry.getString("type")).isEqualTo(type);
        assertThat(entry.getString("actorId")).isEqualTo(actorId);
        assertThat(entry.getDate("at")).isNotNull();
    }

    private AnnotationConfigApplicationContext openContext(MongoClient client) {
        var context = new AnnotationConfigApplicationContext();
        context.registerBean(MongoDatabaseFactory.class, () -> new SimpleMongoClientDatabaseFactory(client, DATABASE));
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
