package com.example.taskflow.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_sessions")
public class UserSession {
    @Id
    @Column(name = "jti", length = 100)
    private String jti;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_sessions_user"))
    private User user;

    @Column(name = "device_hint", length = 200)
    private String deviceHint;

    @Column(name = "login_time", nullable = false)
    private Instant loginTime = Instant.now();

    @Column(name = "last_active", nullable = false)
    private Instant lastActive = Instant.now();

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public UserSession() {}

    public UserSession(String jti, User user, String deviceHint, Instant expiresAt) {
        this.jti = jti;
        this.user = user;
        this.deviceHint = deviceHint;
        this.expiresAt = expiresAt;
    }

    public String getJti() { return jti; }
    public User getUser() { return user; }
    public String getDeviceHint() { return deviceHint; }
    public Instant getLoginTime() { return loginTime; }
    public Instant getLastActive() { return lastActive; }
    public void setLastActive(Instant lastActive) { this.lastActive = lastActive; }
    public Instant getExpiresAt() { return expiresAt; }
}

