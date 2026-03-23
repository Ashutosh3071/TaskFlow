package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateSubtaskRequest {
    @NotBlank
    private String title;
    private Long assignedTo;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }
}

