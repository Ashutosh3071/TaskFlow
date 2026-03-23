package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class CreateTeamRequest {
    @NotBlank
    private String name;
    private String description;
    private List<Long> memberIds;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Long> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds; }
}

