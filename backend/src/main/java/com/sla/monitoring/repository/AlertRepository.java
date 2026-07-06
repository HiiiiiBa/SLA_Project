package com.sla.monitoring.repository;

import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.enums.AlertStatus;
import com.sla.monitoring.entity.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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

    @Query("""
            SELECT a FROM Alert a
            JOIN FETCH a.sla s
            LEFT JOIN FETCH a.service
            ORDER BY a.createdAt DESC
            """)
    List<Alert> findAllWithDetails();

    @Query("""
            SELECT a FROM Alert a
            JOIN FETCH a.sla s
            LEFT JOIN FETCH a.service svc
            WHERE (:slaId IS NULL OR s.id = :slaId)
            AND (:serviceId IS NULL OR svc.id = :serviceId)
            AND (:type IS NULL OR a.type = :type)
            AND (:status IS NULL OR a.status = :status)
            ORDER BY a.createdAt DESC
            """)
    List<Alert> findFiltered(@Param("slaId") Long slaId,
                             @Param("serviceId") Long serviceId,
                             @Param("type") AlertType type,
                             @Param("status") AlertStatus status);

    @Query("""
            SELECT a FROM Alert a
            JOIN FETCH a.sla s
            LEFT JOIN FETCH a.service
            WHERE s.id IN :slaIds
            ORDER BY a.createdAt DESC
            """)
    List<Alert> findBySlaIdIn(@Param("slaIds") Collection<Long> slaIds);
}
