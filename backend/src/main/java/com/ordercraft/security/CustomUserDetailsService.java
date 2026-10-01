package com.ordercraft.security;

import com.ordercraft.entity.User;
import com.ordercraft.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String identifier = username.trim();
        User user = userRepository.findByUsername(identifier)
            .or(() -> userRepository.findByEmail(identifier.toLowerCase(Locale.ROOT)))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return new CustomUserDetails(user);
    }
}
