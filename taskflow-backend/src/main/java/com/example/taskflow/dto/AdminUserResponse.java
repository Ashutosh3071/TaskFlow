package com.example.taskflow.dto;

import java.time.Instant;

public class AdminUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private boolean active;
    private boolean admin;
    private Instant createdAt;

    public AdminUserResponse(Long id, String fullName, String email, boolean active, boolean admin, Instant createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.active = active;
        this.admin = admin;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
    public boolean isAdmin() { return admin; }
    public Instant getCreatedAt() { return createdAt; }
}

