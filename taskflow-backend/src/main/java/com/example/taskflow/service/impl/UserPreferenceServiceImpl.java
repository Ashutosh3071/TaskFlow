package com.example.taskflow.service.impl;

import com.example.taskflow.domain.UserPreference;
import com.example.taskflow.repository.UserPreferenceRepository;
import com.example.taskflow.repository.UserRepository;
import com.example.taskflow.service.UserPreferenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPreferenceServiceImpl implements UserPreferenceService {
    private final UserPreferenceRepository prefs;
    private final UserRepository users;

    public UserPreferenceServiceImpl(UserPreferenceRepository prefs, UserRepository users) {
        this.prefs = prefs;
        this.users = users;
    }

    @Override
    @Transactional
    public UserPreference getOrCreate(Long userId) {
        return prefs.findById(userId).orElseGet(() -> {
            UserPreference p = new UserPreference();
            // attach via managed reference inside this transaction
            p.setUser(users.getReferenceById(userId));
            return prefs.save(p);
        });
    }
}

