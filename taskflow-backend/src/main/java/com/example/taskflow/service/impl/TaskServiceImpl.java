package com.example.taskflow.service.impl;

import com.example.taskflow.domain.Priority;
import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.TaskSummaryResponse;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.exception.ResourceNotFoundException;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.service.ActivityLogService;
import com.example.taskflow.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository tasks;
    private final ActivityLogService activityLogs;

    public TaskServiceImpl(TaskRepository tasks, ActivityLogService activityLogs) {
        this.tasks = tasks;
        this.activityLogs = activityLogs;
    }

    @Override
    public List<Task> findAll(User owner, Priority priority) {
        if (priority == null) {
            return tasks.findAllByOwner(owner);
        }
        if (priority == Priority.MEDIUM || priority == Priority.MED) {
            return tasks.findAllByOwnerAndPriorityIn(owner, List.of(Priority.MED, Priority.MEDIUM));
        }
        return tasks.findAllByOwnerAndPriority(owner, priority);
    }

    @Override
    public TaskSummaryResponse getSummary(User owner) {
        int total = (int) tasks.countByOwner(owner);
        TaskSummaryResponse.ByStatus byStatus = new TaskSummaryResponse.ByStatus(
                (int) tasks.countTodoByOwner(owner),
                (int) tasks.countInProgressByOwner(owner),
                (int) tasks.countDoneByOwner(owner)
        );
        TaskSummaryResponse.ByPriority byPriority = new TaskSummaryResponse.ByPriority(
                (int) tasks.countHighByOwner(owner),
                (int) tasks.countMediumByOwnerId(owner.getId()),
                (int) tasks.countLowByOwner(owner)
        );
        return new TaskSummaryResponse(
                total,
                byStatus,
                byPriority,
                tasks.completionRateByOwnerId(owner.getId()),
                (int) tasks.overdueCountByOwner(owner),
                (int) tasks.countTasksThisWeekByOwnerId(owner.getId()),
                (int) tasks.dueTodayCountByOwner(owner)
        );
    }

    @Override
    public Task create(User owner, Task task) {
        task.setOwner(owner);
        Task saved = tasks.save(task);
        activityLogs.log(saved, owner, "TASK_CREATED",
                owner.getFullName() + " created task \"" + saved.getTitle() + "\"");
        return saved;
    }

    @Override
    public Task findByIdOrThrow(User owner, Long id) {
        return tasks.findById(id)
                .map(t -> {
                    if (!t.getOwner().getId().equals(owner.getId())) {
                        throw new ForbiddenException("You do not own this task");
                    }
                    return t;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    @Override
    public Task update(User owner, Long id, Task updated) {
        Task existing = findByIdOrThrow(owner, id);
        var oldStatus = existing.getStatus();
        var oldPriority = existing.getPriority();
        var oldAssignedToId = existing.getAssignedTo() != null ? existing.getAssignedTo().getId() : null;
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDueDate(updated.getDueDate());
        existing.setStatus(updated.getStatus());
        existing.setPriority(updated.getPriority());
        existing.setAssignedTo(updated.getAssignedTo());
        Task saved = tasks.save(existing);

        if (oldStatus != saved.getStatus()) {
            activityLogs.log(saved, owner, "TASK_STATUS_CHANGED",
                    owner.getFullName() + " changed status of \"" + saved.getTitle() + "\" to " + formatStatus(saved.getStatus().name()));
        }
        if (oldPriority != saved.getPriority()) {
            activityLogs.log(saved, owner, "TASK_PRIORITY_CHANGED",
                    owner.getFullName() + " changed priority of \"" + saved.getTitle() + "\" to " + formatPriority(saved.getPriority().name()));
        }

        Long newAssignedToId = saved.getAssignedTo() != null ? saved.getAssignedTo().getId() : null;
        if ((oldAssignedToId == null && newAssignedToId != null)
                || (oldAssignedToId != null && !oldAssignedToId.equals(newAssignedToId))) {
            String assigneeName = saved.getAssignedTo() != null ? saved.getAssignedTo().getFullName() : "Unassigned";
            activityLogs.log(saved, owner, "TASK_ASSIGNED",
                    owner.getFullName() + " assigned \"" + saved.getTitle() + "\" to " + assigneeName);
        }

        return saved;
    }

    @Override
    public void delete(User owner, Long id) {
        Task existing = findByIdOrThrow(owner, id);
        String title = existing.getTitle();
        tasks.delete(existing);
        activityLogs.log(null, owner, "TASK_DELETED",
                owner.getFullName() + " deleted task \"" + title + "\"");
    }

    private String formatStatus(String status) {
        if ("IN_PROGRESS".equals(status)) return "In Progress";
        if ("TODO".equals(status)) return "To-Do";
        return "Done";
    }

    private String formatPriority(String priority) {
        if ("HIGH".equals(priority)) return "High";
        if ("LOW".equals(priority)) return "Low";
        return "Medium";
    }
}
