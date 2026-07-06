package com.sla.monitoring.repository;

import com.sla.monitoring.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByManagerId(Long managerId);

    @Query("""
            SELECT t FROM Team t
            JOIN FETCH t.manager
            LEFT JOIN FETCH t.members
            WHERE t.id = :id
            """)
    Optional<Team> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT t FROM Team t
            JOIN FETCH t.manager
            LEFT JOIN FETCH t.members
            ORDER BY t.name
            """)
    List<Team> findAllWithDetails();

    @Query("""
            SELECT DISTINCT t FROM Team t
            JOIN FETCH t.manager
            LEFT JOIN FETCH t.members m
            WHERE m.id = :userId
            """)
    List<Team> findByMemberId(@Param("userId") Long userId);
}
