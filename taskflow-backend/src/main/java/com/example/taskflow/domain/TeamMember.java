package com.example.taskflow.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "team_members")
public class TeamMember {
    @EmbeddedId
    private TeamMemberId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("teamId")
    @JoinColumn(name = "team_id", foreignKey = @ForeignKey(name = "fk_team_members_team"))
    private Team team;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_team_members_user"))
    private User user;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt = Instant.now();

    public TeamMember() {}

    public TeamMember(Team team, User user) {
        this.team = team;
        this.user = user;
        this.id = new TeamMemberId(team.getId(), user.getId());
    }

    public TeamMemberId getId() { return id; }
    public Team getTeam() { return team; }
    public User getUser() { return user; }
    public Instant getJoinedAt() { return joinedAt; }
}

