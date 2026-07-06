package com.sla.monitoring.repository;

import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.enums.IncidentSeverity;
import com.sla.monitoring.entity.enums.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findBySlaId(Long slaId);

    List<Incident> findBySeverity(IncidentSeverity severity);

    List<Incident> findByProjectId(Long projectId);

    List<Incident> findByEndTimeIsNull();

    List<Incident> findByStatusNot(IncidentStatus status);

    List<Incident> findBySlaIdAndEndTimeIsNull(Long slaId);

    @Query("""
            SELECT i FROM Incident i
            LEFT JOIN FETCH i.project
            LEFT JOIN FETCH i.assignee
            JOIN FETCH i.sla s
            JOIN FETCH s.client
            ORDER BY i.startTime DESC
            """)
    List<Incident> findAllWithDetails();

    @Query("""
            SELECT i FROM Incident i
            LEFT JOIN FETCH i.project
            LEFT JOIN FETCH i.assignee
            JOIN FETCH i.sla s
            JOIN FETCH s.client
            WHERE i.id = :id
            """)
    Optional<Incident> findByIdWithDetails(Long id);
}
