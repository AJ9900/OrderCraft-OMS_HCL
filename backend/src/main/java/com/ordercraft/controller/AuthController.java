package com.ordercraft.controller;

import com.ordercraft.dto.AuthResponse;
import com.ordercraft.dto.LoginRequest;
import com.ordercraft.entity.User;
import com.ordercraft.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.authenticate(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
