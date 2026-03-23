package com.example.taskflow.controller;

import com.example.taskflow.domain.User;
import com.example.taskflow.dto.*;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    @PostMapping
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody CreateTeamRequest req) {
        TeamResponse created = teamService.createTeam(currentUser(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> list() {
        return ResponseEntity.ok(teamService.listTeams(currentUser()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeam(currentUser(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> update(@PathVariable Long id, @RequestBody UpdateTeamRequest req) {
        return ResponseEntity.ok(teamService.updateTeam(currentUser(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teamService.deleteTeam(currentUser(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<TeamResponse> addMember(@PathVariable Long id, @Valid @RequestBody AddTeamMemberRequest req) {
        return ResponseEntity.ok(teamService.addMember(currentUser(), id, req.getUserId()));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<TeamResponse> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(teamService.removeMember(currentUser(), id, userId));
    }
}

