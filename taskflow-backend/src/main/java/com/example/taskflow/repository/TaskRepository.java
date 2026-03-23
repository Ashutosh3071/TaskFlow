package com.example.taskflow.repository;

import com.example.taskflow.domain.Priority;
import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOwnerAndDeletedFalse(User owner);
    List<Task> findAllByOwnerAndPriorityAndDeletedFalse(User owner, Priority priority);
    List<Task> findAllByOwnerAndPriorityInAndDeletedFalse(User owner, List<Priority> priorities);
    Optional<Task> findByIdAndOwner(Long id, User owner);

    @Query("""
            select distinct t
            from Task t
            left join t.team team
            left join TeamMember tm on tm.team = team and tm.user = :user
            where (t.owner = :user
               or t.assignedTo = :user
               or tm.user = :user)
              and t.deleted = false
            """)
    List<Task> findAllVisibleTo(@Param("user") User user);

    @Query("""
            select distinct t
            from Task t
            left join t.team team
            left join TeamMember tm on tm.team = team and tm.user = :user
            where (t.owner = :user or t.assignedTo = :user or tm.user = :user)
              and t.priority = :priority
              and t.deleted = false
            """)
    List<Task> findAllVisibleToAndPriority(@Param("user") User user, @Param("priority") Priority priority);

    @Query("""
            select distinct t
            from Task t
            left join t.team team
            left join TeamMember tm on tm.team = team and tm.user = :user
            where (t.owner = :user or t.assignedTo = :user or tm.user = :user)
              and t.priority in :priorities
              and t.deleted = false
            """)
    List<Task> findAllVisibleToAndPriorityIn(@Param("user") User user, @Param("priorities") List<Priority> priorities);

    long countByOwnerAndDeletedFalse(User owner);

    @Query("select count(t) from Task t where t.owner = :owner and t.deleted = false and t.status = com.example.taskflow.domain.TaskStatus.TODO")
    long countTodoByOwner(@Param("owner") User owner);

    @Query("select count(t) from Task t where t.owner = :owner and t.deleted = false and t.status = com.example.taskflow.domain.TaskStatus.IN_PROGRESS")
    long countInProgressByOwner(@Param("owner") User owner);

    @Query("select count(t) from Task t where t.owner = :owner and t.deleted = false and t.status = com.example.taskflow.domain.TaskStatus.DONE")
    long countDoneByOwner(@Param("owner") User owner);

    @Query("select count(t) from Task t where t.owner = :owner and t.deleted = false and t.priority = com.example.taskflow.domain.Priority.HIGH")
    long countHighByOwner(@Param("owner") User owner);

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE deleted = false AND user_id = :ownerId AND priority IN ('MED', 'MEDIUM')", nativeQuery = true)
    long countMediumByOwnerId(@Param("ownerId") Long ownerId);

    @Query("select count(t) from Task t where t.owner = :owner and t.deleted = false and t.priority = com.example.taskflow.domain.Priority.LOW")
    long countLowByOwner(@Param("owner") User owner);

    @Query("select count(t) from Task t where t.owner = :owner and t.deleted = false and t.dueDate < CURRENT_DATE and t.status <> com.example.taskflow.domain.TaskStatus.DONE")
    long overdueCountByOwner(@Param("owner") User owner);

    @Query("select count(t) from Task t where t.owner = :owner and t.deleted = false and t.dueDate = CURRENT_DATE and t.status <> com.example.taskflow.domain.TaskStatus.DONE")
    long dueTodayCountByOwner(@Param("owner") User owner);

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE deleted = false AND user_id = :ownerId AND DATE(created_at) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)", nativeQuery = true)
    long countTasksThisWeekByOwnerId(@Param("ownerId") Long ownerId);

    @Query(value = "SELECT COALESCE(ROUND((SUM(CASE WHEN status='DONE' THEN 1 ELSE 0 END) * 100.0) / NULLIF(COUNT(*), 0), 1), 0) FROM tasks WHERE deleted = false AND user_id = :ownerId", nativeQuery = true)
    double completionRateByOwnerId(@Param("ownerId") Long ownerId);
}
