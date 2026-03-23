package com.example.taskflow.repository;

import com.example.taskflow.domain.Team;
import com.example.taskflow.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByNameIgnoreCase(String name);

    List<Team> findAllByManagerAndDeletedFalse(User manager);

    List<Team> findAllByDeletedFalse();

    @Query("""
            select distinct tm.team
            from TeamMember tm
            where tm.user = :user
              and tm.team.deleted = false
            """)
    List<Team> findAllJoinedBy(@Param("user") User user);
}

