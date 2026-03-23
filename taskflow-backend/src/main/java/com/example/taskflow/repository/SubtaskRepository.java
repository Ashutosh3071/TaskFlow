package com.example.taskflow.repository;

import com.example.taskflow.domain.Subtask;
import com.example.taskflow.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
    List<Subtask> findAllByTaskOrderByCreatedAtAsc(Task task);

    @Query("select count(s) from Subtask s where s.task = :task")
    long countByTask(@Param("task") Task task);

    @Query("select count(s) from Subtask s where s.task = :task and s.complete = true")
    long countCompletedByTask(@Param("task") Task task);
}

