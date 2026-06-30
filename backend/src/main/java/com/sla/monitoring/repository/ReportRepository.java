package com.sla.monitoring.repository;

import com.sla.monitoring.entity.Report;
import com.sla.monitoring.entity.enums.ReportFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findBySlaId(Long slaId);

    List<Report> findByFormat(ReportFormat format);

    boolean existsBySlaIdAndPeriodStartAndPeriodEnd(Long slaId, LocalDateTime periodStart, LocalDateTime periodEnd);

    @Query("SELECT r FROM Report r JOIN FETCH r.sla s JOIN FETCH s.client WHERE r.id = :id")
    Optional<Report> findByIdWithSlaAndClient(@Param("id") Long id);
}
