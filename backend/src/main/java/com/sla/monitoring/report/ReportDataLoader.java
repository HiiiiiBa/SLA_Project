package com.sla.monitoring.report;

import com.sla.monitoring.engine.SlaCalculator;
import com.sla.monitoring.engine.model.SlaEvaluationResult;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Report;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.report.model.ReportExportData;
import com.sla.monitoring.repository.IncidentRepository;
import com.sla.monitoring.repository.MonitoringMetricRepository;
import com.sla.monitoring.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Loads report data and SLA evaluation metrics for export.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportDataLoader {

    private final ReportRepository reportRepository;
    private final MonitoringMetricRepository monitoringMetricRepository;
    private final IncidentRepository incidentRepository;
    private final SlaCalculator slaCalculator;

    public ReportExportData load(Long reportId) {
        Report report = reportRepository.findByIdWithSlaAndClient(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

        var sla = report.getSla();
        var client = sla.getClient();
        LocalDateTime periodStart = report.getPeriodStart();
        LocalDateTime periodEnd = report.getPeriodEnd();

        List<MonitoringMetric> metrics = monitoringMetricRepository
                .findBySlaIdAndTimestampBetweenWithService(sla.getId(), periodStart, periodEnd);
        List<Incident> incidents = incidentRepository.findBySlaId(sla.getId()).stream()
                .filter(incident -> overlapsPeriod(incident, periodStart, periodEnd))
                .toList();

        SlaEvaluationResult evaluation = slaCalculator.evaluate(
                sla, metrics, incidents, periodStart, periodEnd);

        return ReportExportData.builder()
                .report(report)
                .sla(sla)
                .client(client)
                .evaluation(evaluation)
                .metrics(metrics)
                .incidents(incidents)
                .build();
    }

    private boolean overlapsPeriod(Incident incident, LocalDateTime periodStart, LocalDateTime periodEnd) {
        LocalDateTime incidentEnd = incident.getEndTime() != null ? incident.getEndTime() : periodEnd;
        return incident.getStartTime().isBefore(periodEnd) && incidentEnd.isAfter(periodStart);
    }
}
