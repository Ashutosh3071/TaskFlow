package com.example.taskflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class TimeLogRequest {
    @NotNull
    @Min(0)
    @Max(24 * 60 * 99)
    private Integer durationMinutes;

    @NotNull
    private LocalDate logDate;

    private String note;

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

