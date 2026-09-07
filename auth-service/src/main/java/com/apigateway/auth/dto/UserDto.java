package com.apigateway.auth.dto;

import java.time.LocalDateTime;

public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String apiKey;
    private LocalDateTime createdAt;
    private boolean active;

    public UserDto() {}

    public UserDto(Long id, String username, String email, String role, String apiKey, LocalDateTime createdAt, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.apiKey = apiKey;
        this.createdAt = createdAt;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
