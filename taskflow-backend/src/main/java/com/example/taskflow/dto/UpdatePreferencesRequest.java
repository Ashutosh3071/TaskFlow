package com.example.taskflow.dto;

public class UpdatePreferencesRequest {
    private String theme; // LIGHT/DARK/SYSTEM
    private Boolean notifyAssigned;
    private Boolean notifyComment;
    private Boolean notifySubtask;
    private Boolean notifyOverdue;
    private Boolean notifyTeam;
    private String avatarColour;
    private String bio;

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public Boolean getNotifyAssigned() { return notifyAssigned; }
    public void setNotifyAssigned(Boolean notifyAssigned) { this.notifyAssigned = notifyAssigned; }
    public Boolean getNotifyComment() { return notifyComment; }
    public void setNotifyComment(Boolean notifyComment) { this.notifyComment = notifyComment; }
    public Boolean getNotifySubtask() { return notifySubtask; }
    public void setNotifySubtask(Boolean notifySubtask) { this.notifySubtask = notifySubtask; }
    public Boolean getNotifyOverdue() { return notifyOverdue; }
    public void setNotifyOverdue(Boolean notifyOverdue) { this.notifyOverdue = notifyOverdue; }
    public Boolean getNotifyTeam() { return notifyTeam; }
    public void setNotifyTeam(Boolean notifyTeam) { this.notifyTeam = notifyTeam; }
    public String getAvatarColour() { return avatarColour; }
    public void setAvatarColour(String avatarColour) { this.avatarColour = avatarColour; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}

