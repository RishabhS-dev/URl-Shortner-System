package com.urlshortener.controller;

import com.urlshortener.dto.ApiResponse;
import com.urlshortener.dto.LoginRequest;
import com.urlshortener.dto.RegisterRequest;
import com.urlshortener.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Body: { "name": "John", "email": "john@example.com", "password": "secret123" }
     * Returns: { "success": true, "message": "...", "data": { "token": "...", "email": "..." } }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(
            @Valid @RequestBody RegisterRequest request) {

        Map<String, String> result = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful! Welcome.", result));
    }

    /**
     * POST /api/auth/login
     * Body: { "email": "john@example.com", "password": "secret123" }
     * Returns: { "success": true, "data": { "token": "eyJ...", "email": "..." } }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(
            @Valid @RequestBody LoginRequest request) {

        Map<String, String> result = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful!", result));
    }
}