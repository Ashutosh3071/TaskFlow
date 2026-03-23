package com.example.taskflow.repository;

import com.example.taskflow.domain.ActiveTimer;
import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActiveTimerRepository extends JpaRepository<ActiveTimer, Long> {
    Optional<ActiveTimer> findByTaskAndUser(Task task, User user);
    boolean existsByTaskAndUser(Task task, User user);
}

