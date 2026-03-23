package com.example.taskflow.service.impl;

import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.TaskComment;
import com.example.taskflow.domain.User;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.repository.TaskCommentRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.service.ActivityLogService;
import com.example.taskflow.service.TaskCommentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskCommentRepository commentRepo;
    private final TaskRepository taskRepo;
    private final ActivityLogService activityLogs;

    public TaskCommentServiceImpl(TaskCommentRepository commentRepo, TaskRepository taskRepo, ActivityLogService activityLogs) {
        this.commentRepo = commentRepo;
        this.taskRepo = taskRepo;
        this.activityLogs = activityLogs;
    }

    @Override
    public List<TaskComment> findByTaskId(Long taskId) {
        return commentRepo.findByTaskIdOrderByCreatedAtAsc(taskId);
    }

    @Override
    public TaskComment create(Long taskId, User author, String body) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (task.isDeleted()) {
            throw new IllegalArgumentException("Task not found");
        }
        TaskComment comment = new TaskComment(task, author, body);
        TaskComment saved = commentRepo.save(comment);
        activityLogs.log(task, author, "COMMENT_ADDED",
                author.getFullName() + " commented on \"" + task.getTitle() + "\"");
        return saved;
    }

    @Override
    public void delete(Long commentId, User requester) {
        TaskComment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        
        // Check if requester is the author
        if (!comment.getAuthor().getId().equals(requester.getId())) {
            throw new ForbiddenException("You can only delete your own comments");
        }
        
        commentRepo.delete(comment);
    }
}
