package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.requests.LoginRequest;
import com.codewithmosh.store.dtos.requests.RegisterUserRequest;
import com.codewithmosh.store.dtos.resources.AuthResource;
import com.codewithmosh.store.services.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.codewithmosh.store.dtos.resources.ApiResponse;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/user/register")
    public ResponseEntity<ApiResponse<AuthResource>> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        var auth = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(auth, "User registered successfully"));
    }

    @PostMapping("/user/login")
    public ResponseEntity<ApiResponse<AuthResource>> loginUser(@Valid @RequestBody LoginRequest request) {
        var auth = authService.loginUser(request);
        return ResponseEntity.ok(ApiResponse.success(auth, "Login successful"));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<AuthResource>> loginAdmin(@Valid @RequestBody LoginRequest request) {
        var auth = authService.loginAdmin(request);
        return ResponseEntity.ok(ApiResponse.success(auth, "Login successful"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage(), null));
    }
}
