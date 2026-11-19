package org.seitmolabs.modules.notification.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.seitmolabs.modules.notification.controller.AdminNotificationController;
import org.seitmolabs.modules.notification.controller.NotificationController;
import org.seitmolabs.modules.notification.factory.NotificationFactory;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
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
        OutboxEvent event = OutboxEvent.builder().id(UUID.randomUUID()).username("client01")
                .title("Заказ подтверждён").message("Заказ №1").build();

        try (MongoClient client = MongoClients.create(uri)) {
            var notifications = client.getDatabase(DATABASE).getCollection("notifications");
            notifications.insertOne(new Document("_id", 41L).append("userId", "other-user"));

            try (var context = openContext(client)) {
                var service = notificationService(context, recipient);
                MockMvcBuilders.standaloneSetup(new AdminNotificationController(service)).build()
                        .perform(post("/api/v1/admin/notifications").contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"userId":"client01","title":"Заголовок","text":"Текст","categoryId":1}
                                        """))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").value(42))
                        .andExpect(jsonPath("$.status").value("SENT"));
                service.deliver(event);
            }

            try (var context = openContext(client)) {
                var service = notificationService(context, recipient);
                service.deliver(event);
                var api = MockMvcBuilders.standaloneSetup(new NotificationController(service)).build();
                api.perform(get("/api/v1/notifications").requestAttr(CURRENT_USER, recipient))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2));
                api.perform(get("/api/v1/notifications/42").requestAttr(CURRENT_USER, recipient))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("SENT"));
                api.perform(post("/api/v1/notifications/42/read").requestAttr(CURRENT_USER, recipient))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("READ"));
                api.perform(delete("/api/v1/notifications/42").requestAttr(CURRENT_USER, recipient))
                        .andExpect(status().isNoContent());
            }

            assertThat(notifications.countDocuments(new Document("_id", 42L))).isZero();
            assertThat(notifications.countDocuments(new Document("sourceEventId", event.getId().toString())))
                    .isEqualTo(1);
        }
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
                new NotificationFactory(), categories);
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableMongoRepositories(basePackageClasses = MongoNotificationRepository.class)
    static class Repositories {
    }
}
