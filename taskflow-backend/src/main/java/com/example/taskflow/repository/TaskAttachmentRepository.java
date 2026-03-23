package com.example.taskflow.repository;

import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    long countByTask(Task task);
    List<TaskAttachment> findAllByTaskOrderByUploadedAtDesc(Task task);
}

