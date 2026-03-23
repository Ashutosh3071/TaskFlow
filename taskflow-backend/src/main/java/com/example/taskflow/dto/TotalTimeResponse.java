package com.example.taskflow.dto;

public class TotalTimeResponse {
    private int totalMinutes;

    public TotalTimeResponse(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public int getTotalMinutes() { return totalMinutes; }
}

