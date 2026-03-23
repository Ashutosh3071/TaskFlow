package com.example.taskflow.repository;

import com.example.taskflow.domain.User;
import com.example.taskflow.domain.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    List<UserSession> findAllByUserOrderByLastActiveDesc(User user);
    void deleteAllByUserAndJtiNot(User user, String jti);
}

