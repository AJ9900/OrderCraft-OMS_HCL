package com.ordercraft.service;

import com.ordercraft.dto.AuthResponse;
import com.ordercraft.dto.LoginRequest;
import com.ordercraft.entity.User;
import com.ordercraft.exception.ResourceNotFoundException;
import com.ordercraft.repository.UserRepository;
import com.ordercraft.security.CustomUserDetails;
import com.ordercraft.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils,
                       UserRepository userRepository,
                       AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public AuthResponse authenticate(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        auditLogService.record("USER_LOGIN", "AUTH", "User", userDetails.getId().toString(), null, "User logged in: " + userDetails.getUsername());

        return new AuthResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFullName(),
                userDetails.getRole()
        );
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found in context");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }
}
