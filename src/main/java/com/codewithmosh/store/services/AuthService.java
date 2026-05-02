package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.requests.LoginRequest;
import com.codewithmosh.store.dtos.requests.RegisterUserRequest;
import com.codewithmosh.store.dtos.resources.AuthResource;
import com.codewithmosh.store.entities.Admin;
import com.codewithmosh.store.entities.User;
import com.codewithmosh.store.repositories.AdminRepository;
import com.codewithmosh.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResource registerUser(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        var token = jwtService.generateToken(user);
        return new AuthResource(token, user.getId(), user.getName(), user.getEmail());
    }

    public AuthResource loginUser(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        var token = jwtService.generateToken(user);
        return new AuthResource(token, user.getId(), user.getName(), user.getEmail());
    }

    public AuthResource loginAdmin(LoginRequest request) {
        var admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        var token = jwtService.generateToken(admin);
        return new AuthResource(token, admin.getId(), admin.getName(), admin.getEmail());
    }
}
