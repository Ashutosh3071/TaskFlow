package com.example.taskflow.service;

import com.example.taskflow.domain.User;
import com.example.taskflow.dto.*;

import java.util.List;

public interface TeamService {
    TeamResponse createTeam(User actor, CreateTeamRequest req);
    List<TeamResponse> listTeams(User actor);
    TeamResponse getTeam(User actor, Long teamId);
    TeamResponse updateTeam(User actor, Long teamId, UpdateTeamRequest req);
    void deleteTeam(User actor, Long teamId);
    TeamResponse addMember(User actor, Long teamId, Long userId);
    TeamResponse removeMember(User actor, Long teamId, Long userId);
}

