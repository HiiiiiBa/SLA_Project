package com.sla.monitoring.repository;

import com.sla.monitoring.entity.MaintenanceWindow;
import com.sla.monitoring.entity.enums.MaintenanceWindowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceWindowRepository extends JpaRepository<MaintenanceWindow, Long> {

    @Query("""
            SELECT mw FROM MaintenanceWindow mw
            JOIN FETCH mw.sla s
            LEFT JOIN FETCH mw.service
            WHERE mw.id = :id
            """)
    Optional<MaintenanceWindow> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT mw FROM MaintenanceWindow mw
            JOIN FETCH mw.sla s
            LEFT JOIN FETCH mw.service
            ORDER BY mw.startTime DESC
            """)
    List<MaintenanceWindow> findAllWithDetails();

    @Query("""
            SELECT mw FROM MaintenanceWindow mw
            JOIN FETCH mw.sla s
            LEFT JOIN FETCH mw.service
            WHERE s.id IN :slaIds
            ORDER BY mw.startTime DESC
            """)
    List<MaintenanceWindow> findBySlaIdInWithDetails(@Param("slaIds") Collection<Long> slaIds);

    @Query("""
            SELECT mw FROM MaintenanceWindow mw
            JOIN FETCH mw.sla s
            LEFT JOIN FETCH mw.service svc
            WHERE (:slaId IS NULL OR s.id = :slaId)
            AND (:status IS NULL OR mw.status = :status)
            ORDER BY mw.startTime DESC
            """)
    List<MaintenanceWindow> findFiltered(@Param("slaId") Long slaId,
                                         @Param("status") MaintenanceWindowStatus status);

    @Query("""
            SELECT mw FROM MaintenanceWindow mw
            WHERE mw.sla.id = :slaId
            AND mw.status <> :cancelled
            AND mw.startTime < :periodEnd
            AND mw.endTime > :periodStart
            """)
    List<MaintenanceWindow> findOverlappingForSla(@Param("slaId") Long slaId,
                                                  @Param("periodStart") LocalDateTime periodStart,
                                                  @Param("periodEnd") LocalDateTime periodEnd,
                                                  @Param("cancelled") MaintenanceWindowStatus cancelled);
}
