package org.seitmolabs.modules.auth.controller;

import org.seitmolabs.modules.auth.dto.request.LoginRequest;
import org.seitmolabs.modules.auth.dto.response.AuthResponse;
import org.seitmolabs.modules.auth.service.AuthService;
import org.seitmolabs.modules.user.domain.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Войти с тестовой учётной записью")
    @SecurityRequirements
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.getUsername(), request.getPassword());
        return AuthResponse.from(user);
    }
}
