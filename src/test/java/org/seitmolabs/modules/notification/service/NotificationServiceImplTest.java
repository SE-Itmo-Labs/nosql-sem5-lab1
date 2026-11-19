package org.seitmolabs.modules.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
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

class NotificationServiceImplTest {

    private MongoNotificationRepository notificationRepository;
    private UserRepository userRepository;
    private NotificationServiceImpl service;
    private CategoryDbRepository categoryDbRepository;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(MongoNotificationRepository.class);
        userRepository = mock(UserRepository.class);
        categoryDbRepository = mock(CategoryDbRepository.class);
        service = new NotificationServiceImpl(
                notificationRepository,
                userRepository,
                new NotificationFactory(),
                categoryDbRepository,
                mock(MongoTemplate.class)
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
