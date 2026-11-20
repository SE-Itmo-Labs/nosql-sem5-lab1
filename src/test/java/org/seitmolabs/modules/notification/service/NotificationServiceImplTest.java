package org.seitmolabs.modules.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.common.exceptions.GlobalExceptionHandler;
import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.seitmolabs.modules.notification.controller.NotificationController;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationAction;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.seitmolabs.modules.notification.factory.NotificationFactory;
import org.seitmolabs.modules.notification.repositories.MongoNotificationRepository;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEventType;
import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.repository.UserRepository;

import org.seitmolabs.common.exceptions.ResourceNotFoundException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class NotificationServiceImplTest {

    private MongoNotificationRepository notificationRepository;
    private UserRepository userRepository;
    private NotificationServiceImpl service;
    private CategoryDbRepository categoryDbRepository;
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(MongoNotificationRepository.class);
        userRepository = mock(UserRepository.class);
        categoryDbRepository = mock(CategoryDbRepository.class);
        mongoTemplate = mock(MongoTemplate.class);
        service = new NotificationServiceImpl(
                notificationRepository,
                userRepository,
                new NotificationFactory(),
                categoryDbRepository,
                mongoTemplate
        );
    }

    @Test
    void createsNotificationForExistingUser() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                "client01",
                "Начало сеанса",
                "Фильм начнётся через 15 минут",
                1L
        );

        when(userRepository.findByUsername("client01")).thenReturn(Optional.of(new User()));
        when(notificationRepository.insert(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(10L);
            return notification;
        });

        Notification result = service.create(request, "admin");

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo("client01");
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getActionLog()).containsExactly(
                new NotificationAction(NotificationAction.Type.CREATED, "admin", result.getCreatedAt(), null));
        verify(notificationRepository).insert(result);
    }

    @Test
    void rejectsUnknownUser() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                "unknown",
                "Заголовок",
                "Текст",
                1L
        );

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request, "admin"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Пользователь не найден: unknown");
    }

    @Test
    void returnsUserNotificationsInRepositoryOrder() {
        Notification first = notification(2L, "Новое");
        Notification second = notification(1L, "Старое");

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("client01")).thenReturn(List.of(first, second));

        List<Notification> result = service.getByUserId("client01");

        assertThat(result).containsExactly(first, second);
    }

    @Test
    void buildsAnOwnerScopedSearchFromHttpParameters() throws Exception {
        User reader = new User();
        reader.setUsername("client01");
        Instant from = Instant.parse("2026-10-08T10:00:00Z");
        Instant to = Instant.parse("2026-10-09T10:00:00Z");
        when(mongoTemplate.count(any(Query.class), eq(Notification.class))).thenAnswer(invocation -> {
            Query count = invocation.getArgument(0);
            assertThat(count.getSkip()).isZero();
            assertThat(count.getLimit()).isZero();
            return 3L;
        });
        when(mongoTemplate.find(any(Query.class), eq(Notification.class))).thenAnswer(invocation -> {
            Query page = invocation.getArgument(0);
            assertThat(page.getQueryObject())
                    .containsEntry("userId", "client01")
                    .containsEntry("actionLog", new Document("$elemMatch",
                            new Document("type", "RULE_APPLIED").append("actorId", "admin")))
                    .containsEntry("updatedAt", new Document("$gte", from).append("$lt", to))
                    .containsEntry("createdAt", new Document("$gte", from).append("$lt", to));
            @SuppressWarnings("unchecked")
            var textFilters = (List<Document>) page.getQueryObject().get("$or");
            var titlePattern = textFilters.getFirst().get("title", Pattern.class);
            assertThat(titlePattern.matcher("ДеМо.*").find()).isTrue();
            assertThat(titlePattern.matcher("Демо любая строка").find()).isFalse();
            assertThat(page.getSortObject()).containsExactly(
                    Map.entry("updatedAt", -1), Map.entry("id", -1));
            assertThat(page.getSkip()).isEqualTo(2);
            assertThat(page.getLimit()).isEqualTo(2);
            return List.of(notification(5L, "Демо"));
        });
        var api = MockMvcBuilders.standaloneSetup(new NotificationController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        api.perform(get("/api/v1/notifications/search")
                        .requestAttr(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE, reader)
                        .param("q", "демо.*").param("userId", "another-user")
                        .param("actionType", "RULE_APPLIED").param("actorId", "admin")
                        .param("updatedFrom", from.toString()).param("updatedTo", to.toString())
                        .param("createdFrom", from.toString()).param("createdTo", to.toString())
                        .param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(5))
                .andExpect(jsonPath("$.items[0].updatedAt").exists())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
        api.perform(get("/api/v1/notifications/search")
                        .requestAttr(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE, reader).param("size", "101"))
                .andExpect(status().isBadRequest());
        api.perform(get("/api/v1/notifications/search")
                        .requestAttr(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE, reader).param("sort", "unknown"))
                .andExpect(status().isBadRequest());
        api.perform(get("/api/v1/notifications/search")
                        .requestAttr(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE, reader)
                        .param("createdFrom", to.toString()).param("createdTo", from.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deliversPendingEventAfterSystemCategoryWasRenamed() {
        Category renamed = new Category(7L, "Мои билеты", "Описание", Category.BOOKING_CODE);
        UUID eventId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.builder().id(eventId).username("client01")
                .eventType(OutboxEventType.ORDER_CONFIRMED)
                .title("Заказ подтверждён").message("Заказ №1")
                .categoryName("Бронирование").build();
        when(categoryDbRepository.findBySystemCode(Category.BOOKING_CODE))
                .thenReturn(Optional.of(renamed));
        service.deliver(event);

        verify(notificationRepository).saveSystem(argThat(notification ->
                notification.getCategoryId().equals(7L)
                        && notification.getUserId().equals("client01")
                        && notification.getSourceEventId().equals(eventId.toString())
                        && notification.getSourceEventType() == OutboxEventType.ORDER_CONFIRMED
                        && notification.getActionLog().equals(List.of(new NotificationAction(
                                NotificationAction.Type.CREATED, "system", notification.getCreatedAt(), null)))));
    }

    private Notification notification(Long id, String title) {
        return new Notification(
                id,
                "client01",
                title,
                "Текст",
                1L,
                NotificationStatus.SENT,
                Instant.parse("2026-10-08T10:00:00Z"),
                null
        );
    }
}
