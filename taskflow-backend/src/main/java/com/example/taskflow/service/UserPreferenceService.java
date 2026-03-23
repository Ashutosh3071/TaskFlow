package com.example.taskflow.service;

import com.example.taskflow.domain.UserPreference;

public interface UserPreferenceService {
    UserPreference getOrCreate(Long userId);
}

