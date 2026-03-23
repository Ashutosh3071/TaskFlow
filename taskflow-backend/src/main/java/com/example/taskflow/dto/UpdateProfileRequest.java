package com.example.taskflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdateProfileRequest {
    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    private String currentPassword;
    private String avatarColour;
    private String bio;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getAvatarColour() { return avatarColour; }
    public void setAvatarColour(String avatarColour) { this.avatarColour = avatarColour; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}

