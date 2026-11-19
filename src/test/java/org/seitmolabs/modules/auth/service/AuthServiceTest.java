package org.seitmolabs.modules.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.seitmolabs.common.exceptions.ConflictException;
import org.seitmolabs.modules.auth.dto.request.RegisterRequest;
import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.enums.Role;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;

class AuthServiceTest {
    @Test
    void registrationReturnsStoredUserWithNumericIdAndClientRole() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.findByUsername("client01")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42L);
            return user;
        });

        User user = new AuthService(repository).register(request());

        assertThat(user.getId()).isEqualTo(42L);
        assertThat(user.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void concurrentRegistrationReturnsConflict() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.findByUsername("client01")).thenReturn(Optional.empty(), Optional.of(User.builder().build()));
        when(repository.save(any(User.class))).thenThrow(new DuplicateKeyException("duplicate username"));

        assertThatThrownBy(() -> new AuthService(repository).register(request()))
                .isInstanceOf(ConflictException.class);
    }

    private RegisterRequest request() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("client01");
        request.setPassword("password");
        request.setDisplayName("Client");
        return request;
    }
}
