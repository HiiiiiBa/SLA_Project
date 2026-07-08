package com.sla.monitoring.report;

import com.sla.monitoring.dto.response.ExecutiveReportKpiSummary;
import com.sla.monitoring.engine.SlaCalculator;
import com.sla.monitoring.engine.model.SlaEvaluationResult;
import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Project;
import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.IncidentSeverity;
import com.sla.monitoring.entity.enums.ServiceStatus;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.report.model.ExecutiveReportContext;
import com.sla.monitoring.repository.AlertRepository;
import com.sla.monitoring.repository.IncidentRepository;
import com.sla.monitoring.repository.MonitoringMetricRepository;
import com.sla.monitoring.repository.ProjectRepository;
import com.sla.monitoring.repository.ServiceRepository;
import com.sla.monitoring.service.ClientScopeService;
import com.sla.monitoring.service.EmployeeScopeService;
import com.sla.monitoring.service.ManagerScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Loads project-scoped KPI aggregates for AI executive reports.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExecutiveReportDataLoader {

    private final ProjectRepository projectRepository;
    private final ServiceRepository serviceRepository;
    private final MonitoringMetricRepository monitoringMetricRepository;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final SlaCalculator slaCalculator;
    private final EmployeeScopeService employeeScopeService;
    private final ManagerScopeService managerScopeService;
    private final ClientScopeService clientScopeService;

    public ExecutiveReportContext load(Long projectId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        if (periodStart == null || periodEnd == null) {
            throw new BusinessException("La période du rapport est obligatoire");
        }
        if (!periodStart.isBefore(periodEnd)) {
            throw new BusinessException("La date de début doit être antérieure à la date de fin");
        }

        employeeScopeService.assertProjectAccess(projectId);
        managerScopeService.assertProjectAccess(projectId);
        clientScopeService.assertProjectAccess(projectId);

        Project project = projectRepository.findByIdWithDetails(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        Sla sla = project.getSla();
        if (sla == null) {
            throw new BusinessException(
                    "Le projet \"" + project.getName() + "\" n'est lié à aucun SLA. Associez un SLA avant de générer le rapport.");
        }

        List<MonitoringMetric> metrics = monitoringMetricRepository
                .findBySlaIdAndTimestampBetweenWithService(sla.getId(), periodStart, periodEnd);
        List<Incident> incidents = incidentRepository.findBySlaId(sla.getId()).stream()
                .filter(incident -> overlapsIncident(incident, periodStart, periodEnd))
                .filter(incident -> incident.getProject() == null
                        || projectId.equals(incident.getProject().getId()))
                .toList();
        List<Alert> alerts = alertRepository.findBySlaId(sla.getId()).stream()
                .filter(alert -> overlapsTimestamp(alert.getCreatedAt(), periodStart, periodEnd))
                .toList();
        List<Service> services = serviceRepository.findBySlaId(sla.getId());

        SlaEvaluationResult evaluation = slaCalculator.evaluate(sla, metrics, incidents, periodStart, periodEnd);

        long criticalIncidents = incidents.stream()
                .filter(incident -> incident.getSeverity() == IncidentSeverity.CRITICAL)
                .count();
        long servicesDown = services.stream()
                .filter(service -> service.getStatus() == ServiceStatus.DOWN)
                .count();
        // Approximation: service currently UP but with elevated error rate during the period
        long servicesDegraded = metrics.stream()
                .filter(metric -> metric.getService() != null
                        && metric.getService().getStatus() != ServiceStatus.DOWN
                        && metric.getErrorRate() != null
                        && metric.getErrorRate() >= Math.max(1.0, sla.getErrorRateLimit() * 0.75))
                .map(metric -> metric.getService().getId())
                .distinct()
                .count();

        ExecutiveReportKpiSummary kpiSummary = ExecutiveReportKpiSummary.builder()
                .slaScore(evaluation.getSlaScore())
                .slaStatus(evaluation.getCurrentStatus().name())
                .uptimePercentage(evaluation.getUptimePercentage())
                .uptimeTarget(sla.getUptimeTarget())
                .averageResponseTime(evaluation.getAverageResponseTime())
                .responseTimeLimit(sla.getResponseTimeLimit() != null
                        ? sla.getResponseTimeLimit().doubleValue()
                        : null)
                .responseTimeCompliance(evaluation.getResponseTimeCompliance())
                .averageErrorRate(evaluation.getAverageErrorRate())
                .errorRateLimit(sla.getErrorRateLimit())
                .incidentCount(incidents.size())
                .criticalIncidentCount((int) criticalIncidents)
                .alertCount(alerts.size())
                .servicesDown((int) servicesDown)
                .servicesDegraded((int) servicesDegraded)
                .metricsAnalyzed(metrics.size())
                .build();

        return ExecutiveReportContext.builder()
                .project(project)
                .sla(sla)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .evaluation(evaluation)
                .kpiSummary(kpiSummary)
                .metrics(metrics)
                .incidents(incidents)
                .alerts(alerts)
                .services(services)
                .build();
    }

    private boolean overlapsTimestamp(LocalDateTime timestamp,
                                      LocalDateTime periodStart,
                                      LocalDateTime periodEnd) {
        return timestamp != null
                && !timestamp.isBefore(periodStart)
                && timestamp.isBefore(periodEnd);
    }

    private boolean overlapsIncident(Incident incident, LocalDateTime periodStart, LocalDateTime periodEnd) {
        LocalDateTime incidentEnd = incident.getEndTime() != null ? incident.getEndTime() : periodEnd;
        return incident.getStartTime().isBefore(periodEnd) && incidentEnd.isAfter(periodStart);
    }
}
