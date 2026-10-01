package com.ordercraft.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${ordercraft.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthEntryPoint unauthorizedHandler;
    private final JwtAuthTokenFilter jwtAuthTokenFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthEntryPoint unauthorizedHandler,
                          JwtAuthTokenFilter jwtAuthTokenFilter) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthTokenFilter = jwtAuthTokenFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toList());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/api/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/audit-logs/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").authenticated()
                        .requestMatchers("/api/dashboard/**").authenticated()
                        .requestMatchers("/api/customers/**").hasAnyRole("ADMIN", "SALES_MANAGER")
                        .requestMatchers("/api/orders/**").hasAnyRole("ADMIN", "SALES_MANAGER", "PRODUCTION_MANAGER")
                        .requestMatchers("/api/products/**").authenticated()
                        .requestMatchers("/api/boms/**").hasAnyRole("ADMIN", "PRODUCTION_MANAGER")
                        .requestMatchers("/api/inventory/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "PRODUCTION_MANAGER", "PROCUREMENT_MANAGER")
                        .requestMatchers("/api/suppliers/**").hasAnyRole("ADMIN", "PROCUREMENT_MANAGER")
                        .requestMatchers("/api/purchase-orders/**").hasAnyRole("ADMIN", "PROCUREMENT_MANAGER", "WAREHOUSE_MANAGER")
                        .requestMatchers("/api/goods-receipts/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "PROCUREMENT_MANAGER")
                        .requestMatchers("/api/production-orders/**").hasAnyRole("ADMIN", "PRODUCTION_MANAGER")
                        .requestMatchers("/api/invoices/**").hasAnyRole("ADMIN", "FINANCE_MANAGER", "SALES_MANAGER")
                        .requestMatchers("/api/payments/**").hasAnyRole("ADMIN", "FINANCE_MANAGER")
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
