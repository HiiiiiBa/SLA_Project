package com.sla.monitoring.repository;

import com.sla.monitoring.entity.IncidentComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentCommentRepository extends JpaRepository<IncidentComment, Long> {

    @Query("""
            SELECT c FROM IncidentComment c
            JOIN FETCH c.author
            WHERE c.incident.id = :incidentId
            ORDER BY c.createdAt ASC
            """)
    List<IncidentComment> findByIncidentIdWithAuthor(Long incidentId);
}
