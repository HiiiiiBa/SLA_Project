package com.sla.monitoring.repository;

import com.sla.monitoring.entity.Project;
import com.sla.monitoring.entity.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByClientId(Long clientId);

    List<Project> findByTeamId(Long teamId);

    long countByClientId(Long clientId);

    @Query("""
            SELECT DISTINCT p FROM Project p
            JOIN FETCH p.client
            LEFT JOIN FETCH p.team t
            LEFT JOIN FETCH t.manager
            LEFT JOIN FETCH p.sla
            LEFT JOIN FETCH p.assignedMembers
            WHERE p.id = :id
            """)
    Optional<Project> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT p FROM Project p
            JOIN FETCH p.client
            LEFT JOIN FETCH p.team t
            LEFT JOIN FETCH t.manager
            LEFT JOIN FETCH p.sla
            LEFT JOIN FETCH p.assignedMembers
            ORDER BY p.name
            """)
    List<Project> findAllWithDetails();

    @Query("""
            SELECT DISTINCT p FROM Project p
            JOIN FETCH p.client
            LEFT JOIN FETCH p.team t
            LEFT JOIN FETCH t.manager
            LEFT JOIN FETCH p.sla
            LEFT JOIN FETCH p.assignedMembers m
            WHERE m.id = :userId
            ORDER BY p.name
            """)
    List<Project> findByAssignedMemberId(@Param("userId") Long userId);

    Optional<Project> findByName(String name);

    List<Project> findByStatus(ProjectStatus status);

    @Query("""
            SELECT DISTINCT p FROM Project p
            LEFT JOIN FETCH p.assignedMembers
            WHERE p.sla.id = :slaId
            """)
    List<Project> findBySlaIdWithMembers(@Param("slaId") Long slaId);
}
