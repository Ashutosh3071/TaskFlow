package com.example.taskflow.controller;

import com.example.taskflow.domain.ActiveTimer;
import com.example.taskflow.domain.TaskTimeLog;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.*;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.exception.ResourceNotFoundException;
import com.example.taskflow.repository.ActiveTimerRepository;
import com.example.taskflow.repository.TaskTimeLogRepository;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TimeTrackingController {
    private final TaskService taskService;
    private final ActiveTimerRepository timers;
    private final TaskTimeLogRepository logs;

    public TimeTrackingController(TaskService taskService, ActiveTimerRepository timers, TaskTimeLogRepository logs) {
        this.taskService = taskService;
        this.timers = timers;
        this.logs = logs;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    private TimeLogResponse toResponse(TaskTimeLog l) {
        return new TimeLogResponse(
                l.getId(),
                l.getTask().getId(),
                l.getLoggedBy().getId(),
                l.getLoggedBy().getFullName(),
                l.getDurationMinutes(),
                l.getLogDate(),
                l.getNote(),
                l.isManual(),
                l.getCreatedAt()
        );
    }

    @PostMapping("/tasks/{taskId}/timer/start")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public ResponseEntity<Void> start(@PathVariable Long taskId) {
        User actor = currentUser();
        var task = taskService.findByIdOrThrow(actor, taskId);
        if (timers.existsByTaskAndUser(task, actor)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        ActiveTimer t = new ActiveTimer();
        t.setTask(task);
        t.setUser(actor);
        t.setStartTime(Instant.now());
        timers.save(t);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/tasks/{taskId}/timer/stop")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public ResponseEntity<TimeLogResponse> stop(@PathVariable Long taskId) {
        User actor = currentUser();
        var task = taskService.findByIdOrThrow(actor, taskId);
        ActiveTimer t = timers.findByTaskAndUser(task, actor).orElseThrow(() -> new ResourceNotFoundException("No active timer"));
        int minutes = (int) Math.max(0, Duration.between(t.getStartTime(), Instant.now()).toMinutes());
        timers.delete(t);

        TaskTimeLog l = new TaskTimeLog();
        l.setTask(task);
        l.setLoggedBy(actor);
        l.setDurationMinutes(minutes);
        l.setLogDate(LocalDate.now());
        l.setManual(false);
        TaskTimeLog saved = logs.save(l);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PostMapping("/tasks/{taskId}/time-logs")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public ResponseEntity<TimeLogResponse> manual(@PathVariable Long taskId, @Valid @RequestBody TimeLogRequest req) {
        User actor = currentUser();
        var task = taskService.findByIdOrThrow(actor, taskId);
        TaskTimeLog l = new TaskTimeLog();
        l.setTask(task);
        l.setLoggedBy(actor);
        l.setDurationMinutes(req.getDurationMinutes());
        l.setLogDate(req.getLogDate());
        l.setNote(req.getNote());
        l.setManual(true);
        TaskTimeLog saved = logs.save(l);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/tasks/{taskId}/time-logs")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER','VIEWER')")
    public ResponseEntity<List<TimeLogResponse>> list(@PathVariable Long taskId) {
        User actor = currentUser();
        var task = taskService.findByIdOrThrow(actor, taskId);
        var list = logs.findAllByTaskOrderByLogDateDesc(task).stream().map(this::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/tasks/{taskId}/time-logs/total")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER','VIEWER')")
    public ResponseEntity<TotalTimeResponse> total(@PathVariable Long taskId) {
        User actor = currentUser();
        var task = taskService.findByIdOrThrow(actor, taskId);
        return ResponseEntity.ok(new TotalTimeResponse(logs.totalMinutesByTask(task)));
    }

    @DeleteMapping("/time-logs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public ResponseEntity<Void> deleteManual(@PathVariable Long id) {
        User actor = currentUser();
        TaskTimeLog l = logs.findById(id).orElseThrow(() -> new ResourceNotFoundException("Time log not found"));
        taskService.findByIdOrThrow(actor, l.getTask().getId());
        if (!l.isManual()) {
            throw new ForbiddenException("Timer entries cannot be deleted");
        }
        if (!l.getLoggedBy().getId().equals(actor.getId()) && actor.getRole().name().equals("MEMBER")) {
            throw new ForbiddenException("You do not have permission to delete this entry");
        }
        logs.delete(l);
        return ResponseEntity.noContent().build();
    }
}

