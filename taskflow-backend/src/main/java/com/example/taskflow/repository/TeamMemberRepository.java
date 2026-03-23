package com.example.taskflow.repository;

import com.example.taskflow.domain.Team;
import com.example.taskflow.domain.TeamMember;
import com.example.taskflow.domain.TeamMemberId;
import com.example.taskflow.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {
    List<TeamMember> findAllByTeamOrderByJoinedAtAsc(Team team);

    boolean existsByTeamAndUser(Team team, User user);

    Optional<TeamMember> findByTeamAndUser(Team team, User user);
}

