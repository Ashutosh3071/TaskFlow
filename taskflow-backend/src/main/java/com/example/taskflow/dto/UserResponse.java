package com.example.taskflow.dto;

public class UserResponse {
    private Long id;
    private String fullName;

    public UserResponse() {}

    public UserResponse(Long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }

    public void setId(Long id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
