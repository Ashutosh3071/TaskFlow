package com.example.taskflow.controller;

import com.example.taskflow.domain.Role;
import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.TaskAttachment;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.AttachmentListItemResponse;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.exception.ResourceNotFoundException;
import com.example.taskflow.repository.TaskAttachmentRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class AttachmentController {
    private static final long MAX_BYTES = 5L * 1024L * 1024L;
    private static final long MAX_FILES = 5;
    private static final Set<String> ALLOWED_MIME = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel",
            "text/plain",
            "application/zip"
    );

    private final TaskRepository tasks;
    private final TaskService taskService;
    private final TaskAttachmentRepository attachments;

    public AttachmentController(TaskRepository tasks, TaskService taskService, TaskAttachmentRepository attachments) {
        this.tasks = tasks;
        this.taskService = taskService;
        this.attachments = attachments;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    @PostMapping(value = "/tasks/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public ResponseEntity<Void> upload(@PathVariable Long taskId,
                                       @RequestParam("file") MultipartFile file,
                                       HttpServletRequest request) throws Exception {
        User actor = currentUser();
        Task task = taskService.findByIdOrThrow(actor, taskId); // access check

        long existing = attachments.countByTask(task);
        if (existing >= MAX_FILES) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("File exceeds 5 MB limit");
        }
        String mime = file.getContentType();
        if (!StringUtils.hasText(mime) || !ALLOWED_MIME.contains(mime)) {
            throw new IllegalArgumentException("File type not allowed");
        }

        TaskAttachment a = new TaskAttachment();
        a.setTask(task);
        a.setUploader(actor);
        a.setOriginalName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "attachment");
        a.setMimeType(mime);
        a.setFileSizeBytes(file.getSize());
        a.setFileData(file.getBytes());
        attachments.save(a);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/tasks/{taskId}/attachments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER','VIEWER')")
    public ResponseEntity<List<AttachmentListItemResponse>> list(@PathVariable Long taskId) {
        User actor = currentUser();
        Task task = taskService.findByIdOrThrow(actor, taskId);
        List<AttachmentListItemResponse> result = attachments.findAllByTaskOrderByUploadedAtDesc(task).stream()
                .map(a -> new AttachmentListItemResponse(
                        a.getId(),
                        task.getId(),
                        a.getUploader().getId(),
                        a.getOriginalName(),
                        a.getMimeType(),
                        a.getFileSizeBytes(),
                        a.getUploadedAt()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/attachments/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER','VIEWER')")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        User actor = currentUser();
        TaskAttachment a = attachments.findById(id).orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
        // ensure actor can access the task
        taskService.findByIdOrThrow(actor, a.getTask().getId());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + a.getOriginalName().replace("\"", "") + "\"")
                .body(a.getFileData());
    }

    @DeleteMapping("/attachments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User actor = currentUser();
        TaskAttachment a = attachments.findById(id).orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
        // ensure actor can access the task
        taskService.findByIdOrThrow(actor, a.getTask().getId());

        boolean isOwner = a.getUploader().getId().equals(actor.getId());
        boolean elevated = actor.getRole() == Role.ADMIN || actor.getRole() == Role.MANAGER;
        if (!isOwner && !elevated) {
            throw new ForbiddenException("You do not have permission to delete this attachment");
        }
        attachments.delete(a);
        return ResponseEntity.noContent().build();
    }
}

