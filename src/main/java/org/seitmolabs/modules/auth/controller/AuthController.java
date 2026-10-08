package org.seitmolabs.modules.auth.controller;

import java.util.Map;

import org.seitmolabs.modules.auth.dto.request.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        if ("admin".equals(request.getUsername()) && "123".equals(request.getPassword())) {
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "username", "admin"
            ));
        }
        return ResponseEntity.status(401).body(Map.of(
                "error", "Invalid username or password"
        ));
    }
}
