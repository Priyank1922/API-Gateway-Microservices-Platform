package com.apigateway.auth.controller;

import com.apigateway.auth.dto.*;
import com.apigateway.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "status", 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage(), "status", 500));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage(), "status", 401));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login error: " + e.getMessage(), "status", 500));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody ValidateTokenRequest request) {
        boolean valid = authService.validateToken(request.getToken());
        Map<String, Object> body = new HashMap<>();
        body.put("valid", valid);
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/verify-key")
    public ResponseEntity<?> verifyApiKey(@RequestParam String apiKey) {
        return authService.getUserByApiKey(apiKey)
                .map(user -> ResponseEntity.ok((Object) Map.of("valid", true, "user", user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "error", "Invalid API Key")));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam String username) {
        return authService.getUserProfile(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PostMapping("/regenerate-key")
    public ResponseEntity<?> regenerateKey(@RequestParam String username) {
        try {
            String newKey = authService.regenerateApiKey(username);
            return ResponseEntity.ok(Map.of("username", username, "apiKey", newKey));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "service", "auth-service",
                "status", "UP",
                "port", 8081,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
