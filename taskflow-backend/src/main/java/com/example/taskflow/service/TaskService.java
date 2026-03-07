package com.example.taskflow.service;

import com.example.taskflow.domain.Priority;
import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.TaskSummaryResponse;

import java.util.List;

public interface TaskService {
    List<Task> findAll(User owner, Priority priority);
    TaskSummaryResponse getSummary(User owner);
    Task create(User owner, Task task);
    Task findByIdOrThrow(User owner, Long id);
    Task update(User owner, Long id, Task updated);
    void delete(User owner, Long id);
}
