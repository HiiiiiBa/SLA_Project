package com.sla.monitoring.repository;

import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.enums.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findBySlaId(Long slaId);

    List<Alert> findByStatus(AlertStatus status);

    List<Alert> findByType(com.sla.monitoring.entity.enums.AlertType type);

    @Query("""
            SELECT a FROM Alert a
            JOIN FETCH a.sla s
            JOIN FETCH s.client
            WHERE a.id = :id
            """)
    Optional<Alert> findByIdWithSlaAndClient(@Param("id") Long id);
}
