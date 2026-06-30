package com.sla.monitoring.repository;

import com.sla.monitoring.entity.MonitoringMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MonitoringMetricRepository extends JpaRepository<MonitoringMetric, Long> {

    List<MonitoringMetric> findBySlaId(Long slaId);

    List<MonitoringMetric> findByServiceId(Long serviceId);

    List<MonitoringMetric> findBySlaIdAndTimestampBetween(Long slaId, LocalDateTime start, LocalDateTime end);

    List<MonitoringMetric> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT m FROM MonitoringMetric m
            JOIN FETCH m.service
            WHERE m.sla.id = :slaId
            AND m.timestamp BETWEEN :start AND :end
            ORDER BY m.timestamp ASC
            """)
    List<MonitoringMetric> findBySlaIdAndTimestampBetweenWithService(
            @Param("slaId") Long slaId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
