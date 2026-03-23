package com.example.taskflow.dto;

import java.time.Instant;

public class SubtaskResponse {
    private Long id;
    private Long taskId;
    private String title;
    private boolean complete;
    private Long assignedToId;
    private String assignedToName;
    private Long createdBy;
    private Instant createdAt;
    private Instant completedAt;

    public SubtaskResponse(Long id, Long taskId, String title, boolean complete,
                           Long assignedToId, String assignedToName,
                           Long createdBy,
                           Instant createdAt, Instant completedAt) {
        this.id = id;
        this.taskId = taskId;
        this.title = title;
        this.complete = complete;
        this.assignedToId = assignedToId;
        this.assignedToName = assignedToName;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public Long getId() { return id; }
    public Long getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public boolean isComplete() { return complete; }
    public Long getAssignedToId() { return assignedToId; }
    public String getAssignedToName() { return assignedToName; }
    public Long getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
}

