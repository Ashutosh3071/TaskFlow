package com.example.taskflow.config;

import com.example.taskflow.domain.User;
import com.example.taskflow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;

@Configuration
public class AdminBootstrapConfig {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapConfig.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public AdminBootstrapConfig(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @PostConstruct
    public void ensureAdminUser() {
        if (adminEmail == null || adminEmail.isBlank()) {
            log.warn("Admin email not configured (app.admin.email). Skipping admin bootstrap.");
            return;
        }

        User admin = users.findByEmail(adminEmail).orElseGet(() -> {
            User u = new User();
            u.setFullName("TaskFlow Admin");
            u.setEmail(adminEmail);
            return u;
        });

        admin.setPasswordHash(encoder.encode(adminPassword));
        admin.setAdmin(true);
        admin.setActive(true);

        users.save(admin);

        log.info("Admin user ensured: email={}, id={}", admin.getEmail(), admin.getId());
    }
}

