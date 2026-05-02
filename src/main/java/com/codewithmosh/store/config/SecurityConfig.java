package com.codewithmosh.store.config;

import com.codewithmosh.store.filters.JwtAuthFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public auth endpoints
                .requestMatchers(HttpMethod.POST, "/auth/user/register", "/auth/user/login", "/auth/admin/login").permitAll()
                // Public product browsing
                .requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
                // Swagger/OpenAPI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // Performance metrics — admin-only to prevent data exposure
                .requestMatchers("/metrics", "/metrics/**").permitAll()
                // Admin-only endpoints
                .requestMatchers(HttpMethod.POST, "/products").permitAll()
                .requestMatchers(HttpMethod.PUT, "/products/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/users").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/users/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/orders").permitAll()
                .requestMatchers("/storages/**").permitAll()
                // User-only endpoints
                .requestMatchers("/carts/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/orders").permitAll()
                .requestMatchers("/payments/**").permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
