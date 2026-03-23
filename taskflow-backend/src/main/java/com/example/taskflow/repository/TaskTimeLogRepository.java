package com.example.taskflow.repository;

import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.TaskTimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskTimeLogRepository extends JpaRepository<TaskTimeLog, Long> {
    List<TaskTimeLog> findAllByTaskOrderByLogDateDesc(Task task);

    @Query("select coalesce(sum(t.durationMinutes), 0) from TaskTimeLog t where t.task = :task")
    int totalMinutesByTask(@Param("task") Task task);
}

