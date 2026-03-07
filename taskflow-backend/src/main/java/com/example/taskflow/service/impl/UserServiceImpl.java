package com.example.taskflow.service.impl;

import com.example.taskflow.domain.User;
import com.example.taskflow.exception.DuplicateEmailException;
import com.example.taskflow.repository.UserRepository;
import com.example.taskflow.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserServiceImpl(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public User register(String fullName, String email, String rawPassword) {
        if (users.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already registered");
        }
        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(rawPassword));
        return users.save(u);
    }

    @Override
    public User findByEmail(String email) {
        return users.findByEmail(email).orElse(null);
    }
}