package com.example.taskflow.controller;

import com.example.taskflow.domain.User;
import com.example.taskflow.domain.Role;
import com.example.taskflow.dto.*;
import com.example.taskflow.security.JwtTokenService;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.domain.UserSession;
import com.example.taskflow.repository.UserSessionRepository;
import com.example.taskflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;
    private final AuthenticationManager authManager;
    private final JwtTokenService jwt;
    private final UserSessionRepository sessions;

    public AuthController(UserService users, AuthenticationManager authManager, JwtTokenService jwt, UserSessionRepository sessions) {
        this.users = users;
        this.authManager = authManager;
        this.jwt = jwt;
        this.sessions = sessions;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        users.register(req.getFullName(), req.getEmail(), req.getPassword());
        // On success, 201 Created as per SRS
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User u = principal.getUser();
        Role role = u.getRole();

        String token = jwt.generateToken(
                u.getEmail(),
                Map.of(
                        "userId", u.getId(),
                        "fullName", u.getFullName(),
                        "role", role.name()
                )
        );

        // Track session (best-effort)
        try {
            var claims = jwt.getClaims(token);
            String jti = claims.get("jti", String.class);
            var expiresAt = jwt.getExpirationInstant(claims);
            String hint = req.getEmail(); // fallback (overwritten in filter via UA isn't available here)
            sessions.save(new UserSession(jti, u, hint, expiresAt));
        } catch (Exception ignored) {}

        return ResponseEntity.ok(new AuthResponse(token, u.getId(), u.getFullName(), u.getEmail(), role.name()));
    }
}
