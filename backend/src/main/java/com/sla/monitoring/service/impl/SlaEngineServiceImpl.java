package com.sla.monitoring.service.impl;

import com.sla.monitoring.config.SlaEngineProperties;
import com.sla.monitoring.dto.response.SlaEvaluationResponse;
import com.sla.monitoring.engine.SlaCalculator;
import com.sla.monitoring.engine.model.SlaEvaluationResult;
import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Report;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.AlertStatus;
import com.sla.monitoring.entity.enums.AlertType;
import com.sla.monitoring.entity.enums.ReportFormat;
import com.sla.monitoring.entity.enums.SlaStatus;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.notification.AlertNotificationService;
import com.sla.monitoring.repository.AlertRepository;
import com.sla.monitoring.repository.IncidentRepository;
import com.sla.monitoring.repository.MonitoringMetricRepository;
import com.sla.monitoring.repository.ReportRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.service.SlaEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Runs SLA evaluations and persists status changes, alerts and reports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlaEngineServiceImpl implements SlaEngineService {

    private final SlaRepository slaRepository;
    private final MonitoringMetricRepository monitoringMetricRepository;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final ReportRepository reportRepository;
    private final AlertNotificationService alertNotificationService;
    private final SlaCalculator slaCalculator;
    private final SlaEngineProperties slaEngineProperties;

    @Override
    @Transactional
    public List<SlaEvaluationResponse> evaluateAll() {
        List<Sla> slas = slaRepository.findByStatusNot(SlaStatus.ARCHIVED);
        log.info("Starting SLA evaluation for {} contract(s)", slas.size());

        return slas.stream()
                .map(this::evaluateSla)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SlaEvaluationResponse evaluateById(Long slaId) {
        Sla sla = findSlaById(slaId);
        if (sla.getStatus() == SlaStatus.ARCHIVED) {
            throw new ResourceNotFoundException("Active SLA", "id", slaId);
        }
        return toResponse(evaluateSla(sla));
    }

    private SlaEvaluationResult evaluateSla(Sla sla) {
        LocalDateTime periodEnd = LocalDateTime.now();
        LocalDateTime periodStart = periodEnd.minusHours(slaEngineProperties.getEvaluationPeriodHours());

        List<MonitoringMetric> metrics = monitoringMetricRepository
                .findBySlaIdAndTimestampBetween(sla.getId(), periodStart, periodEnd);
        List<Incident> incidents = incidentRepository.findBySlaId(sla.getId()).stream()
                .filter(incident -> overlapsPeriod(incident, periodStart, periodEnd))
                .toList();

        SlaEvaluationResult result = slaCalculator.evaluate(sla, metrics, incidents, periodStart, periodEnd);

        if (sla.getStatus() != result.getCurrentStatus()) {
            sla.setStatus(result.getCurrentStatus());
            slaRepository.save(sla);
            log.info("SLA '{}' status changed: {} -> {}", sla.getName(),
                    result.getPreviousStatus(), result.getCurrentStatus());
        }

        boolean alertCreated = false;
        if (slaEngineProperties.isAutoCreateAlerts() && shouldCreateAlert(result)) {
            Alert savedAlert = alertRepository.save(buildAlert(sla, result));
            alertNotificationService.dispatch(savedAlert.getId());
            alertCreated = true;
        }

        boolean reportCreated = false;
        if (slaEngineProperties.isAutoGenerateReports()) {
            reportCreated = createReportIfAbsent(sla, result);
        }

        return SlaEvaluationResult.builder()
                .slaId(result.getSlaId())
                .slaName(result.getSlaName())
                .previousStatus(result.getPreviousStatus())
                .currentStatus(result.getCurrentStatus())
                .uptimePercentage(result.getUptimePercentage())
                .averageResponseTime(result.getAverageResponseTime())
                .averageErrorRate(result.getAverageErrorRate())
                .responseTimeCompliance(result.getResponseTimeCompliance())
                .slaScore(result.getSlaScore())
                .periodStart(result.getPeriodStart())
                .periodEnd(result.getPeriodEnd())
                .metricsAnalyzed(result.getMetricsAnalyzed())
                .incidentsAnalyzed(result.getIncidentsAnalyzed())
                .statusChanged(result.isStatusChanged())
                .alertCreated(alertCreated)
                .reportCreated(reportCreated)
                .build();
    }

    private boolean shouldCreateAlert(SlaEvaluationResult result) {
        return result.isStatusChanged()
                && (result.getCurrentStatus() == SlaStatus.WARNING
                || result.getCurrentStatus() == SlaStatus.BREACHED);
    }

    private Alert buildAlert(Sla sla, SlaEvaluationResult result) {
        return Alert.builder()
                .type(AlertType.WEB)
                .message(buildAlertMessage(result))
                .status(AlertStatus.NEW)
                .sla(sla)
                .build();
    }

    private String buildAlertMessage(SlaEvaluationResult result) {
        return String.format(
                "SLA '%s' status changed to %s. Uptime: %.2f%%, avg response: %.2f ms, avg error rate: %.2f%%, score: %.2f",
                result.getSlaName(),
                result.getCurrentStatus(),
                result.getUptimePercentage(),
                result.getAverageResponseTime(),
                result.getAverageErrorRate(),
                result.getSlaScore()
        );
    }

    private boolean createReportIfAbsent(Sla sla, SlaEvaluationResult result) {
        if (reportRepository.existsBySlaIdAndPeriodStartAndPeriodEnd(
                sla.getId(), result.getPeriodStart(), result.getPeriodEnd())) {
            return false;
        }

        Report report = Report.builder()
                .slaResult(result.getSlaScore())
                .periodStart(result.getPeriodStart())
                .periodEnd(result.getPeriodEnd())
                .generatedAt(LocalDateTime.now())
                .format(ReportFormat.PDF)
                .sla(sla)
                .build();
        reportRepository.save(report);
        return true;
    }

    private boolean overlapsPeriod(Incident incident, LocalDateTime periodStart, LocalDateTime periodEnd) {
        LocalDateTime incidentEnd = incident.getEndTime() != null ? incident.getEndTime() : periodEnd;
        return incident.getStartTime().isBefore(periodEnd) && incidentEnd.isAfter(periodStart);
    }

    private Sla findSlaById(Long slaId) {
        return slaRepository.findById(slaId)
                .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", slaId));
    }

    private SlaEvaluationResponse toResponse(SlaEvaluationResult result) {
        return SlaEvaluationResponse.builder()
                .slaId(result.getSlaId())
                .slaName(result.getSlaName())
                .previousStatus(result.getPreviousStatus())
                .currentStatus(result.getCurrentStatus())
                .uptimePercentage(result.getUptimePercentage())
                .averageResponseTime(result.getAverageResponseTime())
                .averageErrorRate(result.getAverageErrorRate())
                .responseTimeCompliance(result.getResponseTimeCompliance())
                .slaScore(result.getSlaScore())
                .periodStart(result.getPeriodStart())
                .periodEnd(result.getPeriodEnd())
                .metricsAnalyzed(result.getMetricsAnalyzed())
                .incidentsAnalyzed(result.getIncidentsAnalyzed())
                .statusChanged(result.isStatusChanged())
                .alertCreated(result.isAlertCreated())
                .reportCreated(result.isReportCreated())
                .build();
    }
}
