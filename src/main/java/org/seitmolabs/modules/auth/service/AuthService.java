package org.seitmolabs.modules.auth.service;

import org.seitmolabs.common.exceptions.ConflictException;
import org.seitmolabs.modules.auth.dto.request.LoginRequest;
import org.seitmolabs.modules.auth.dto.request.RegisterRequest;
import org.seitmolabs.modules.auth.exceptions.RegistrationException;
import org.seitmolabs.modules.auth.exceptions.UnauthorizedException;
import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.enums.Role;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final String USER_INVALID = "Invalid username or password";
    private final String USER_EXISTS = "User already exists";

    public User authenticate(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException(USER_INVALID));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new UnauthorizedException(USER_INVALID);
        }

        return user;
    }

    public User register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException(USER_EXISTS);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .displayName(request.getDisplayName())
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        return user;
    }
}
