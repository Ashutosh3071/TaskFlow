package com.example.taskflow.controller;

import com.example.taskflow.domain.Subtask;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.CreateSubtaskRequest;
import com.example.taskflow.dto.SubtaskResponse;
import com.example.taskflow.dto.SubtaskSummaryResponse;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.exception.ResourceNotFoundException;
import com.example.taskflow.repository.SubtaskRepository;
import com.example.taskflow.repository.UserRepository;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SubtaskController {
    private final TaskService taskService;
    private final SubtaskRepository subtasks;
    private final UserRepository users;

    public SubtaskController(TaskService taskService, SubtaskRepository subtasks, UserRepository users) {
        this.taskService = taskService;
        this.subtasks = subtasks;
        this.users = users;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    private SubtaskResponse toResponse(Subtask s) {
        return new SubtaskResponse(
                s.getId(),
                s.getTask().getId(),
                s.getTitle(),
                s.isComplete(),
                s.getAssignedTo() != null ? s.getAssignedTo().getId() : null,
                s.getAssignedTo() != null ? s.getAssignedTo().getFullName() : null,
                s.getCreatedBy().getId(),
                s.getCreatedAt(),
                s.getCompletedAt()
        );
    }

    @GetMapping("/tasks/{taskId}/subtasks")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER','VIEWER')")
    public ResponseEntity<List<SubtaskResponse>> list(@PathVariable Long taskId) {
        User actor = currentUser();
        var task = taskService.findByIdOrThrow(actor, taskId);
        var list = subtasks.findAllByTaskOrderByCreatedAtAsc(task).stream().map(this::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/tasks/{taskId}/subtasks")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public ResponseEntity<SubtaskResponse> create(@PathVariable Long taskId, @Valid @RequestBody CreateSubtaskRequest req) {
        User actor = currentUser();
        var task = taskService.findByIdOrThrow(actor, taskId);
        Subtask s = new Subtask();
        s.setTask(task);
        s.setTitle(req.getTitle().trim());
        s.setCreatedBy(actor);
        if (req.getAssignedTo() != null) {
            User assignee = users.findById(req.getAssignedTo())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            s.setAssignedTo(assignee);
        }
        Subtask saved = subtasks.save(s);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PatchMapping("/subtasks/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public ResponseEntity<SubtaskResponse> toggle(@PathVariable Long id) {
        User actor = currentUser();
        Subtask s = subtasks.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));
        // task access check
        taskService.findByIdOrThrow(actor, s.getTask().getId());
        boolean next = !s.isComplete();
        s.setComplete(next);
        s.setCompletedAt(next ? Instant.now() : null);
        Subtask saved = subtasks.save(s);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/subtasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public ResponseEntity<SubtaskResponse> update(@PathVariable Long id, @RequestBody CreateSubtaskRequest req) {
        User actor = currentUser();
        Subtask s = subtasks.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));
        // task access check
        taskService.findByIdOrThrow(actor, s.getTask().getId());

        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            s.setTitle(req.getTitle().trim());
        }
        if (req.getAssignedTo() != null) {
            User assignee = users.findById(req.getAssignedTo())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            s.setAssignedTo(assignee);
        }
        Subtask saved = subtasks.save(s);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/subtasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User actor = currentUser();
        Subtask s = subtasks.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));
        taskService.findByIdOrThrow(actor, s.getTask().getId());

        boolean elevated = actor.getRole().name().equals("ADMIN") || actor.getRole().name().equals("MANAGER");
        boolean creator = s.getCreatedBy().getId().equals(actor.getId());
        if (!elevated && !creator) {
            throw new ForbiddenException("You do not have permission to delete this subtask");
        }
        subtasks.delete(s);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tasks/{taskId}/subtasks/summary")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER','VIEWER')")
    public ResponseEntity<SubtaskSummaryResponse> summary(@PathVariable Long taskId) {
        User actor = currentUser();
        var task = taskService.findByIdOrThrow(actor, taskId);
        int total = (int) subtasks.countByTask(task);
        int completed = (int) subtasks.countCompletedByTask(task);
        return ResponseEntity.ok(new SubtaskSummaryResponse(total, completed));
    }
}

