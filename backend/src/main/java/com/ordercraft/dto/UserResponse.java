package com.ordercraft.dto;

import com.ordercraft.entity.Role;
import com.ordercraft.entity.User;

import java.time.LocalDateTime;

public record UserResponse(Long id, String username, String email, String fullName, Role role,
                           boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
                user.getRole(), user.isActive(), user.getCreatedAt(), user.getUpdatedAt());
    }
}