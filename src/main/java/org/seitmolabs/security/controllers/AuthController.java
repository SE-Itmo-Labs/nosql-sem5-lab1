package org.seitmolabs.security.controllers;

import org.seitmolabs.modules.auth.dto.response.AuthResponse;
import org.seitmolabs.modules.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.seitmolabs.modules.auth.dto.request.RegistgerRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "Регистрация и авторизация")
public class AuthController {

    // private final AuthService authService;

    // @PostMapping("/register")
    // public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    //     return ResponseEntity.ok(authService.register(request));
    // }

    // @PostMapping("/login")
    // public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    //     return ResponseEntity.ok(authService.login(request));
    // }
}