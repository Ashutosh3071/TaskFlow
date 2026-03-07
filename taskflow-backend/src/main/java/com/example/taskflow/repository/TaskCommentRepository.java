package com.example.taskflow.repository;

import com.example.taskflow.domain.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    @Query("""
        select c
        from TaskComment c
        join fetch c.task t
        join fetch c.author a
        where t.id = :taskId
        order by c.createdAt asc
    """)
    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(@Param("taskId") Long taskId);
    Optional<TaskComment> findByIdAndAuthorId(Long commentId, Long authorId);
}
