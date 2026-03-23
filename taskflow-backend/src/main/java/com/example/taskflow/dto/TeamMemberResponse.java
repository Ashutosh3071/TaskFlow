package com.example.taskflow.dto;

import java.time.Instant;

public class TeamMemberResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private Instant joinedAt;

    public TeamMemberResponse(Long userId, String fullName, String email, String role, Instant joinedAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Instant getJoinedAt() { return joinedAt; }
}

