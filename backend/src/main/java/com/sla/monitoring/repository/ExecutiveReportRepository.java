package com.sla.monitoring.repository;

import com.sla.monitoring.entity.ExecutiveReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExecutiveReportRepository extends JpaRepository<ExecutiveReport, Long> {

    @Query("""
            SELECT e FROM ExecutiveReport e
            LEFT JOIN FETCH e.project
            LEFT JOIN FETCH e.sla
            LEFT JOIN FETCH e.generatedBy
            WHERE e.id = :id
            """)
    Optional<ExecutiveReport> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT e FROM ExecutiveReport e
            ORDER BY e.generatedAt DESC
            """)
    List<ExecutiveReport> findAllOrderByGeneratedAtDesc();

    @Query("""
            SELECT e FROM ExecutiveReport e
            WHERE e.project.id IN :projectIds
            ORDER BY e.generatedAt DESC
            """)
    List<ExecutiveReport> findByProjectIdInOrderByGeneratedAtDesc(
            @Param("projectIds") Collection<Long> projectIds);

    @Query("""
            SELECT e FROM ExecutiveReport e
            WHERE e.project.id = :projectId
            ORDER BY e.generatedAt DESC
            """)
    List<ExecutiveReport> findByProjectIdOrderByGeneratedAtDesc(@Param("projectId") Long projectId);
}
