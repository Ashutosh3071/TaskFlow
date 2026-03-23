package com.example.taskflow.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "active_timers", uniqueConstraints = @UniqueConstraint(name = "uk_active_timers_task_user", columnNames = {"task_id", "user_id"}))
public class ActiveTimer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", foreignKey = @ForeignKey(name = "fk_active_timers_task"))
    private Task task;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_active_timers_user"))
    private User user;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    public Long getId() { return id; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
}

