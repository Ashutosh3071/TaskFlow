package com.example.taskflow.dto;

import java.time.Instant;
import java.util.List;

public class TeamResponse {
    private Long id;
    private String name;
    private String description;
    private Long managerId;
    private String managerName;
    private String managerEmail;
    private String managerRole;
    private Instant createdAt;
    private List<TeamMemberResponse> members;

    public TeamResponse(Long id, String name, String description,
                        Long managerId, String managerName, String managerEmail, String managerRole,
                        Instant createdAt,
                        List<TeamMemberResponse> members) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.managerId = managerId;
        this.managerName = managerName;
        this.managerEmail = managerEmail;
        this.managerRole = managerRole;
        this.createdAt = createdAt;
        this.members = members;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getManagerId() { return managerId; }
    public String getManagerName() { return managerName; }
    public String getManagerEmail() { return managerEmail; }
    public String getManagerRole() { return managerRole; }
    public Instant getCreatedAt() { return createdAt; }
    public List<TeamMemberResponse> getMembers() { return members; }
}

