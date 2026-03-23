package com.example.taskflow.controller;

import com.example.taskflow.domain.Role;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.AdminUserResponse;
import com.example.taskflow.dto.ChangeUserRoleRequest;
import com.example.taskflow.dto.ChangeUserStatusRequest;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.repository.UserRepository;
import com.example.taskflow.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository users;

    public AdminUserController(UserRepository users) {
        this.users = users;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    private void assertAdmin() {
        User u = currentUser();
        if (!u.isAdmin()) {
            throw new ForbiddenException("Only admin can perform this action");
        }
    }

    private User getTargetOrThrow(Long id) {
        return users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> listAll() {
        assertAdmin();
        List<AdminUserResponse> result = users.findAll().stream()
                .map(u -> new AdminUserResponse(
                        u.getId(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getRole().name(),
                        u.isActive(),
                        u.getCreatedAt()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> changeRole(@PathVariable Long id, @RequestBody ChangeUserRoleRequest req) {
        assertAdmin();
        User target = getTargetOrThrow(id);
        Role next;
        try {
            next = Role.valueOf(req.getRole().trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid role");
        }
        target.setRole(next);
        users.save(target);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestBody ChangeUserStatusRequest req) {
        assertAdmin();
        User target = getTargetOrThrow(id);
        if (target.getRole() == Role.ADMIN && !req.isActive()) {
            throw new ForbiddenException("Admin account cannot be deactivated");
        }
        target.setActive(req.isActive());
        users.save(target);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        assertAdmin();
        User target = getTargetOrThrow(id);
        if (target.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Admin account cannot be deactivated");
        }
        target.setActive(false);
        users.save(target);
        return ResponseEntity.noContent().build();
    }
}

