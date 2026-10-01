package com.ordercraft.service;

import com.ordercraft.dto.UserRequest;
import com.ordercraft.dto.UserResponse;
import com.ordercraft.entity.User;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserManagementService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                 AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username is already in use");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already in use");
        }

        User user = new User(username, passwordEncoder.encode(request.getPassword()), email,
                request.getFullName().trim(), request.getRole());
        User saved = userRepository.save(user);
        auditLogService.record("USER_CREATED", "USERS", "User", saved.getId().toString(), null,
                saved.getUsername() + " (" + saved.getRole() + ")");
        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse setUserActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.ordercraft.exception.ResourceNotFoundException("User not found with ID: " + id));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!active && authentication != null && user.getUsername().equals(authentication.getName())) {
            throw new BadRequestException("You cannot deactivate your own account");
        }
        boolean oldValue = user.isActive();
        user.setActive(active);
        User saved = userRepository.save(user);
        auditLogService.record(active ? "USER_ACTIVATED" : "USER_DEACTIVATED", "USERS", "User", id.toString(),
                Boolean.toString(oldValue), Boolean.toString(active));
        return UserResponse.from(saved);
    }
}