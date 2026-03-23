package com.example.taskflow.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "teams", uniqueConstraints = @UniqueConstraint(name = "uk_teams_name", columnNames = "name"))
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Lob
    private String description;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "manager_id", foreignKey = @ForeignKey(name = "fk_teams_manager"))
    private User manager;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getManager() { return manager; }
    public void setManager(User manager) { this.manager = manager; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}

