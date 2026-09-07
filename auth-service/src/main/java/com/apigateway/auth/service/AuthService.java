package com.apigateway.auth.service;

import com.apigateway.auth.dto.*;
import com.apigateway.auth.entity.User;
import com.apigateway.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        String role = (request.getRole() != null && !request.getRole().isBlank()) 
                ? request.getRole().toUpperCase() 
                : "ROLE_USER";
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        String apiKey = generateRandomApiKey();
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(request.getUsername(), request.getEmail(), encodedPassword, role, apiKey);
        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getUsername(), savedUser.getRole(), savedUser.getId());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getApiKey(),
                jwtService.getExpirationMs(),
                "User registered successfully"
        );
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        if (!user.isActive()) {
            throw new IllegalStateException("User account is disabled");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole(), user.getId());

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getApiKey(),
                jwtService.getExpirationMs(),
                "Authentication successful"
        );
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    public Optional<UserDto> getUserByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey).map(this::toDto);
    }

    public Optional<UserDto> getUserProfile(String username) {
        return userRepository.findByUsername(username).map(this::toDto);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public String regenerateApiKey(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        String newKey = generateRandomApiKey();
        user.setApiKey(newKey);
        userRepository.save(user);
        return newKey;
    }

    public String generateRandomApiKey() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return "ak_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRole(),
                u.getApiKey(),
                u.getCreatedAt(),
                u.isActive()
        );
    }
}
