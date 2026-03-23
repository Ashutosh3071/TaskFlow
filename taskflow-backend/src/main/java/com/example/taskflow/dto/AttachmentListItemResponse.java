package com.example.taskflow.dto;

import java.time.Instant;

public class AttachmentListItemResponse {
    private Long id;
    private Long taskId;
    private Long uploaderId;
    private String originalName;
    private String mimeType;
    private long fileSizeBytes;
    private Instant uploadedAt;

    public AttachmentListItemResponse(Long id, Long taskId, Long uploaderId, String originalName, String mimeType, long fileSizeBytes, Instant uploadedAt) {
        this.id = id;
        this.taskId = taskId;
        this.uploaderId = uploaderId;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }
    public Long getTaskId() { return taskId; }
    public Long getUploaderId() { return uploaderId; }
    public String getOriginalName() { return originalName; }
    public String getMimeType() { return mimeType; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public Instant getUploadedAt() { return uploadedAt; }
}

