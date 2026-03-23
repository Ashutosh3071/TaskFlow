package com.example.taskflow.controller;

import com.example.taskflow.domain.TokenBlocklist;
import com.example.taskflow.domain.User;
import com.example.taskflow.domain.UserPreference;
import com.example.taskflow.dto.*;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.repository.TokenBlocklistRepository;
import com.example.taskflow.repository.UserPreferenceRepository;
import com.example.taskflow.repository.UserRepository;
import com.example.taskflow.repository.UserSessionRepository;
import com.example.taskflow.security.JwtTokenService;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.UserPreferenceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/users/me")
public class MeController {
    private final UserRepository users;
    private final UserPreferenceRepository prefs;
    private final UserPreferenceService prefService;
    private final PasswordEncoder encoder;
    private final UserSessionRepository sessions;
    private final TokenBlocklistRepository blocklist;
    private final JwtTokenService jwt;

    public MeController(UserRepository users,
                        UserPreferenceRepository prefs,
                        UserPreferenceService prefService,
                        PasswordEncoder encoder,
                        UserSessionRepository sessions,
                        TokenBlocklistRepository blocklist,
                        JwtTokenService jwt) {
        this.users = users;
        this.prefs = prefs;
        this.prefService = prefService;
        this.encoder = encoder;
        this.sessions = sessions;
        this.blocklist = blocklist;
        this.jwt = jwt;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    private User currentManagedUser() {
        User u = currentUser();
        return users.findById(u.getId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private String currentJti(HttpServletRequest req) {
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) return null;
        try {
            var claims = jwt.getClaims(header.substring(7));
            return claims.get("jti", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    private UserPreference prefOrCreate(User managedUser) {
        return prefService.getOrCreate(managedUser.getId());
    }

    @GetMapping
    public ResponseEntity<MeResponse> me() {
        User u = currentManagedUser();
        UserPreference p = prefOrCreate(u);
        return ResponseEntity.ok(new MeResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name(), p.getAvatarColour(), p.getBio()));
    }

    @PatchMapping("/profile")
    public ResponseEntity<MeResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        User u = currentManagedUser();
        boolean changingEmail = req.getEmail() != null && !req.getEmail().equalsIgnoreCase(u.getEmail());
        if (changingEmail) {
            if (!StringUtils.hasText(req.getCurrentPassword()) || !encoder.matches(req.getCurrentPassword(), u.getPasswordHash())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
            if (users.existsByEmail(req.getEmail())) {
                throw new IllegalArgumentException("Email already registered");
            }
            u.setEmail(req.getEmail());
        }
        u.setFullName(req.getFullName());
        users.save(u);

        UserPreference p = prefOrCreate(u);
        if (StringUtils.hasText(req.getAvatarColour())) p.setAvatarColour(req.getAvatarColour());
        if (req.getBio() != null) p.setBio(req.getBio());
        prefs.save(p);

        return ResponseEntity.ok(new MeResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name(), p.getAvatarColour(), p.getBio()));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        User u = currentManagedUser();
        if (!encoder.matches(req.getCurrentPassword(), u.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        u.setPasswordHash(encoder.encode(req.getNewPassword()));
        users.save(u);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/preferences")
    public ResponseEntity<Void> updatePreferences(@RequestBody UpdatePreferencesRequest req) {
        User u = currentManagedUser();
        UserPreference p = prefOrCreate(u);
        if (req.getTheme() != null) {
            try {
                p.setTheme(UserPreference.Theme.valueOf(req.getTheme().trim().toUpperCase()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid theme");
            }
        }
        if (req.getNotifyAssigned() != null) p.setNotifyAssigned(req.getNotifyAssigned());
        if (req.getNotifyComment() != null) p.setNotifyComment(req.getNotifyComment());
        if (req.getNotifySubtask() != null) p.setNotifySubtask(req.getNotifySubtask());
        if (req.getNotifyOverdue() != null) p.setNotifyOverdue(req.getNotifyOverdue());
        if (req.getNotifyTeam() != null) p.setNotifyTeam(req.getNotifyTeam());
        if (req.getAvatarColour() != null) p.setAvatarColour(req.getAvatarColour());
        if (req.getBio() != null) p.setBio(req.getBio());
        prefs.save(p);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMe(@RequestBody(required = false) java.util.Map<String, String> body) {
        User u = currentManagedUser();
        String confirmEmail = body != null ? body.get("confirmEmail") : null;
        if (confirmEmail == null || !confirmEmail.equalsIgnoreCase(u.getEmail())) {
            throw new IllegalArgumentException("Email confirmation does not match");
        }
        // Soft-delete user: deactivate (keeps FK integrity)
        u.setActive(false);
        users.save(u);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> listSessions(HttpServletRequest req) {
        User u = currentManagedUser();
        String current = currentJti(req);
        var list = sessions.findAllByUserOrderByLastActiveDesc(u).stream()
                .map(s -> new SessionResponse(s.getJti(), s.getDeviceHint(), s.getLoginTime(), s.getLastActive(), s.getExpiresAt(), s.getJti().equals(current)))
                .toList();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/sessions/{jti}")
    public ResponseEntity<Void> revokeSession(@PathVariable String jti, HttpServletRequest req) {
        User u = currentManagedUser();
        var s = sessions.findById(jti).orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!s.getUser().getId().equals(u.getId())) {
            throw new ForbiddenException("Cannot revoke another user's session");
        }
        blocklist.save(new TokenBlocklist(jti, u, s.getExpiresAt()));
        sessions.deleteById(jti);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<Void> revokeAllOtherSessions(HttpServletRequest req) {
        User u = currentManagedUser();
        String current = currentJti(req);
        if (current == null) return ResponseEntity.noContent().build();
        var list = sessions.findAllByUserOrderByLastActiveDesc(u);
        for (var s : list) {
            if (s.getJti().equals(current)) continue;
            blocklist.save(new TokenBlocklist(s.getJti(), u, s.getExpiresAt()));
            sessions.deleteById(s.getJti());
        }
        return ResponseEntity.noContent().build();
    }
}

