package org.seitmolabs.modules.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.seitmolabs.modules.notification.repositories.NotificationRepository;
import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

class NotificationServiceImplTest {

    private NotificationRepository notificationRepository;
    private UserRepository userRepository;
    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        userRepository = mock(UserRepository.class);
        service = new NotificationServiceImpl(notificationRepository, userRepository);
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
        when(notificationRepository.nextId()).thenReturn(10L);

        Notification result = service.create(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo("client01");
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.getCreatedAt()).isNotNull();
        verify(notificationRepository).save(result);
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

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Пользователь не найден: unknown");
    }

    @Test
    void returnsUserNotificationsInRepositoryOrder() {
        Notification first = notification(2L, "Новое");
        Notification second = notification(1L, "Старое");

        when(notificationRepository.findIdsByUserId("client01")).thenReturn(List.of(2L, 1L));
        when(notificationRepository.findById(2L)).thenReturn(Optional.of(first));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(second));

        List<Notification> result = service.getByUserId("client01");

        assertThat(result).containsExactly(first, second);
    }

    private Notification notification(Long id, String title) {
        return new Notification(
                id,
                "client01",
                title,
                "Текст",
                1L,
                NotificationStatus.SENT,
                Instant.parse("2026-10-08T10:00:00Z")
        );
    }
}
