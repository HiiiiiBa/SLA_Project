package com.sla.monitoring.repository;

import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.enums.IncidentSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findBySlaId(Long slaId);

    List<Incident> findBySeverity(IncidentSeverity severity);

    List<Incident> findByProjectId(Long projectId);

    List<Incident> findByEndTimeIsNull();

    List<Incident> findBySlaIdAndEndTimeIsNull(Long slaId);

    @Query("""
            SELECT i FROM Incident i
            LEFT JOIN FETCH i.project
            JOIN FETCH i.sla s
            JOIN FETCH s.client
            ORDER BY i.startTime DESC
            """)
    List<Incident> findAllWithDetails();
}
