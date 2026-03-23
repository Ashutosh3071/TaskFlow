package com.example.taskflow.service.impl;

import com.example.taskflow.domain.*;
import com.example.taskflow.dto.*;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.exception.ResourceNotFoundException;
import com.example.taskflow.repository.TeamMemberRepository;
import com.example.taskflow.repository.TeamRepository;
import com.example.taskflow.repository.UserRepository;
import com.example.taskflow.service.TeamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teams;
    private final TeamMemberRepository teamMembers;
    private final UserRepository users;

    public TeamServiceImpl(TeamRepository teams, TeamMemberRepository teamMembers, UserRepository users) {
        this.teams = teams;
        this.teamMembers = teamMembers;
        this.users = users;
    }

    private boolean isAdmin(User u) { return u.getRole() == Role.ADMIN; }
    private boolean isManager(User u) { return u.getRole() == Role.MANAGER || u.getRole() == Role.ADMIN; }

    private Team getTeamOrThrow(Long id) {
        Team t = teams.findById(id).orElseThrow(() -> new ResourceNotFoundException("Team not found"));
        if (t.isDeleted()) throw new ResourceNotFoundException("Team not found");
        return t;
    }

    private void assertCanManageTeam(User actor, Team team) {
        if (isAdmin(actor)) return;
        if (actor.getRole() == Role.MANAGER && team.getManager().getId().equals(actor.getId())) return;
        throw new ForbiddenException("You do not have permission to manage this team");
    }

    private void assertCanViewTeam(User actor, Team team) {
        if (isAdmin(actor)) return;
        if (team.getManager().getId().equals(actor.getId())) return;
        boolean isMember = teamMembers.existsByTeamAndUser(team, actor);
        if (isMember) return;
        throw new ForbiddenException("You do not have permission to view this team");
    }

    private TeamResponse toResponse(Team team, List<TeamMember> members) {
        var mgr = team.getManager();
        List<TeamMemberResponse> memberDtos = members.stream()
                .map(tm -> new TeamMemberResponse(
                        tm.getUser().getId(),
                        tm.getUser().getFullName(),
                        tm.getUser().getEmail(),
                        tm.getUser().getRole().name(),
                        tm.getJoinedAt()
                ))
                .toList();
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                mgr.getId(),
                mgr.getFullName(),
                mgr.getEmail(),
                mgr.getRole().name(),
                team.getCreatedAt(),
                memberDtos
        );
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public TeamResponse createTeam(User actor, CreateTeamRequest req) {
        if (teams.existsByNameIgnoreCase(req.getName())) {
            throw new IllegalArgumentException("Team name already exists");
        }
        Team t = new Team();
        t.setName(req.getName().trim());
        t.setDescription(req.getDescription());
        t.setManager(actor);
        Team saved = teams.save(t);

        // Ensure manager is a member
        if (!teamMembers.existsByTeamAndUser(saved, actor)) {
            teamMembers.save(new TeamMember(saved, actor));
        }

        if (req.getMemberIds() != null) {
            for (Long uid : req.getMemberIds()) {
                if (uid == null) continue;
                User u = users.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User not found"));
                if (!teamMembers.existsByTeamAndUser(saved, u)) {
                    teamMembers.save(new TeamMember(saved, u));
                }
            }
        }

        return toResponse(saved, teamMembers.findAllByTeamOrderByJoinedAtAsc(saved));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER','VIEWER')")
    public List<TeamResponse> listTeams(User actor) {
        List<Team> relevant;
        if (isAdmin(actor)) {
            relevant = teams.findAllByDeletedFalse();
        } else if (actor.getRole() == Role.MANAGER) {
            relevant = teams.findAllByManagerAndDeletedFalse(actor);
        } else {
            relevant = teams.findAllJoinedBy(actor);
        }
        return relevant.stream()
                .map(t -> toResponse(t, teamMembers.findAllByTeamOrderByJoinedAtAsc(t)))
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER','VIEWER')")
    public TeamResponse getTeam(User actor, Long teamId) {
        Team team = getTeamOrThrow(teamId);
        assertCanViewTeam(actor, team);
        return toResponse(team, teamMembers.findAllByTeamOrderByJoinedAtAsc(team));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public TeamResponse updateTeam(User actor, Long teamId, UpdateTeamRequest req) {
        Team team = getTeamOrThrow(teamId);
        assertCanManageTeam(actor, team);

        if (req.getName() != null && !req.getName().isBlank()) {
            String nextName = req.getName().trim();
            if (!nextName.equalsIgnoreCase(team.getName()) && teams.existsByNameIgnoreCase(nextName)) {
                throw new IllegalArgumentException("Team name already exists");
            }
            team.setName(nextName);
        }
        if (req.getDescription() != null) {
            team.setDescription(req.getDescription());
        }
        if (req.getManagerId() != null) {
            User newManager = users.findById(req.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            if (newManager.getRole() != Role.MANAGER && newManager.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Manager must have MANAGER or ADMIN role");
            }
            team.setManager(newManager);
            if (!teamMembers.existsByTeamAndUser(team, newManager)) {
                teamMembers.save(new TeamMember(team, newManager));
            }
        }
        Team saved = teams.save(team);
        return toResponse(saved, teamMembers.findAllByTeamOrderByJoinedAtAsc(saved));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void deleteTeam(User actor, Long teamId) {
        Team team = getTeamOrThrow(teamId);
        assertCanManageTeam(actor, team);
        team.setDeleted(true);
        teams.save(team);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public TeamResponse addMember(User actor, Long teamId, Long userId) {
        Team team = getTeamOrThrow(teamId);
        assertCanManageTeam(actor, team);
        User u = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!teamMembers.existsByTeamAndUser(team, u)) {
            teamMembers.save(new TeamMember(team, u));
        }
        return toResponse(team, teamMembers.findAllByTeamOrderByJoinedAtAsc(team));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public TeamResponse removeMember(User actor, Long teamId, Long userId) {
        Team team = getTeamOrThrow(teamId);
        assertCanManageTeam(actor, team);
        User u = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (team.getManager().getId().equals(u.getId())) {
            throw new IllegalArgumentException("Cannot remove the team manager");
        }
        teamMembers.findByTeamAndUser(team, u).ifPresent(teamMembers::delete);
        return toResponse(team, teamMembers.findAllByTeamOrderByJoinedAtAsc(team));
    }
}

