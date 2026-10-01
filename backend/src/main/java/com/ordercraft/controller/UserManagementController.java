package com.ordercraft.controller;

import com.ordercraft.dto.UserRequest;
import com.ordercraft.dto.UserResponse;
import com.ordercraft.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserManagementController {

    private final UserManagementService userService;

    public UserManagementController(UserManagementService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<UserResponse> setUserActive(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(userService.setUserActive(id, active));
    }
}