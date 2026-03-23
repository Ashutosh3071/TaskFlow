package com.example.taskflow.dto;

import java.time.Instant;

public class AdminUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private boolean active;
    private Instant createdAt;

    public AdminUserResponse(Long id, String fullName, String email, String role, boolean active, Instant createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}

