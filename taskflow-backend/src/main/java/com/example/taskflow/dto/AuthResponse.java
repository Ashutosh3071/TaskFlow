package com.example.taskflow.dto;

public class AuthResponse {
    private String token;
    private Long userId;
    private String fullName;
    private String email;
    private boolean admin;

    public AuthResponse(String token, Long userId, String fullName, String email, boolean admin) {
        this.token = token;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.admin = admin;
    }

    // Getters
    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public boolean isAdmin() { return admin; }
}