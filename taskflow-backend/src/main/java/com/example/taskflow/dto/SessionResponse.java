package com.example.taskflow.dto;

import java.time.Instant;

public class SessionResponse {
    private String jti;
    private String deviceHint;
    private Instant loginTime;
    private Instant lastActive;
    private Instant expiresAt;
    private boolean current;

    public SessionResponse(String jti, String deviceHint, Instant loginTime, Instant lastActive, Instant expiresAt, boolean current) {
        this.jti = jti;
        this.deviceHint = deviceHint;
        this.loginTime = loginTime;
        this.lastActive = lastActive;
        this.expiresAt = expiresAt;
        this.current = current;
    }

    public String getJti() { return jti; }
    public String getDeviceHint() { return deviceHint; }
    public Instant getLoginTime() { return loginTime; }
    public Instant getLastActive() { return lastActive; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isCurrent() { return current; }
}

