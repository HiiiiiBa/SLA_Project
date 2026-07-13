package com.sla.monitoring.engine;

import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.MaintenanceWindow;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.MetricStatus;
import com.sla.monitoring.entity.enums.SlaStatus;
import com.sla.monitoring.engine.model.SlaEvaluationResult;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pure SLA calculation logic based on monitoring metrics and incidents.
 */
@Component
public class SlaCalculator {

    private static final double UPTIME_WEIGHT = 0.50;
    private static final double RESPONSE_WEIGHT = 0.25;
    private static final double ERROR_WEIGHT = 0.25;
    private static final double WARNING_RESPONSE_RATIO = 0.90;
    private static final double WARNING_ERROR_RATIO = 0.90;
    private static final double WARNING_UPTIME_MARGIN = 0.50;

    /**
     * Evaluates SLA compliance over the given period (no maintenance windows).
     */
    public SlaEvaluationResult evaluate(Sla sla,
                                        List<MonitoringMetric> metrics,
                                        List<Incident> incidents,
                                        LocalDateTime periodStart,
                                        LocalDateTime periodEnd) {
        return evaluate(sla, metrics, incidents, List.of(), periodStart, periodEnd);
    }

    /**
     * Evaluates SLA compliance over the given period, excluding maintenance windows.
     */
    public SlaEvaluationResult evaluate(Sla sla,
                                        List<MonitoringMetric> metrics,
                                        List<Incident> incidents,
                                        List<MaintenanceWindow> maintenanceWindows,
                                        LocalDateTime periodStart,
                                        LocalDateTime periodEnd) {
        List<MaintenanceWindow> windows = maintenanceWindows == null ? List.of() : maintenanceWindows;

        long periodMinutes = Math.max(1, Duration.between(periodStart, periodEnd).toMinutes());
        long maintenanceMinutes = calculateMaintenanceMinutes(windows, periodStart, periodEnd);
        long effectivePeriodMinutes = Math.max(1, periodMinutes - maintenanceMinutes);

        List<MonitoringMetric> effectiveMetrics = metrics.stream()
                .filter(metric -> !isInsideMaintenance(metric.getTimestamp(), windows))
                .toList();

        double uptimePercentage = calculateUptimePercentage(
                effectiveMetrics, incidents, windows, periodStart, periodEnd, effectivePeriodMinutes);
        double averageResponseTime = calculateAverageResponseTime(effectiveMetrics);
        double averageErrorRate = calculateAverageErrorRate(effectiveMetrics);
        double responseTimeCompliance = calculateResponseTimeCompliance(effectiveMetrics, sla.getResponseTimeLimit());
        double slaScore = calculateSlaScore(sla, uptimePercentage, averageResponseTime, averageErrorRate, effectiveMetrics);

        SlaStatus currentStatus = determineStatus(
                sla,
                uptimePercentage,
                averageResponseTime,
                averageErrorRate,
                effectiveMetrics
        );

        return SlaEvaluationResult.builder()
                .slaId(sla.getId())
                .slaName(sla.getName())
                .previousStatus(sla.getStatus())
                .currentStatus(currentStatus)
                .uptimePercentage(round(uptimePercentage))
                .averageResponseTime(round(averageResponseTime))
                .averageErrorRate(round(averageErrorRate))
                .responseTimeCompliance(round(responseTimeCompliance))
                .slaScore(round(slaScore))
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .metricsAnalyzed(effectiveMetrics.size())
                .incidentsAnalyzed(incidents.size())
                .maintenanceMinutesExcluded(maintenanceMinutes)
                .statusChanged(sla.getStatus() != currentStatus)
                .alertCreated(false)
                .reportCreated(false)
                .build();
    }

    private double calculateUptimePercentage(List<MonitoringMetric> metrics,
                                             List<Incident> incidents,
                                             List<MaintenanceWindow> windows,
                                             LocalDateTime periodStart,
                                             LocalDateTime periodEnd,
                                             long effectivePeriodMinutes) {
        double incidentDowntimeMinutes = calculateIncidentDowntimeMinutes(
                incidents, windows, periodStart, periodEnd);
        double incidentBasedUptime = Math.max(
                0.0,
                100.0 - (incidentDowntimeMinutes / effectivePeriodMinutes * 100.0));

        if (metrics.isEmpty()) {
            return incidentBasedUptime;
        }

        long upSamples = metrics.stream()
                .filter(metric -> metric.getStatus() == MetricStatus.UP)
                .count();
        double metricBasedUptime = (upSamples / (double) metrics.size()) * 100.0;

        return Math.min(incidentBasedUptime, metricBasedUptime);
    }

    private double calculateIncidentDowntimeMinutes(List<Incident> incidents,
                                                    List<MaintenanceWindow> windows,
                                                    LocalDateTime periodStart,
                                                    LocalDateTime periodEnd) {
        return incidents.stream()
                .mapToLong(incident -> {
                    LocalDateTime incidentEnd = incident.getEndTime() != null
                            ? incident.getEndTime()
                            : periodEnd;
                    long raw = overlapMinutes(
                            incident.getStartTime(), incidentEnd, periodStart, periodEnd);
                    long excluded = windows.stream()
                            .mapToLong(window -> overlapOfThree(
                                    incident.getStartTime(),
                                    incidentEnd,
                                    window.getStartTime(),
                                    window.getEndTime(),
                                    periodStart,
                                    periodEnd))
                            .sum();
                    return Math.max(0, raw - Math.min(excluded, raw));
                })
                .sum();
    }

    private long calculateMaintenanceMinutes(List<MaintenanceWindow> windows,
                                             LocalDateTime periodStart,
                                             LocalDateTime periodEnd) {
        return windows.stream()
                .mapToLong(window -> overlapMinutes(
                        window.getStartTime(), window.getEndTime(), periodStart, periodEnd))
                .sum();
    }

    private boolean isInsideMaintenance(LocalDateTime timestamp, List<MaintenanceWindow> windows) {
        if (timestamp == null) {
            return false;
        }
        return windows.stream().anyMatch(window ->
                !timestamp.isBefore(window.getStartTime()) && timestamp.isBefore(window.getEndTime()));
    }

    private long overlapOfThree(LocalDateTime aStart,
                                LocalDateTime aEnd,
                                LocalDateTime bStart,
                                LocalDateTime bEnd,
                                LocalDateTime periodStart,
                                LocalDateTime periodEnd) {
        LocalDateTime start = latest(aStart, bStart, periodStart);
        LocalDateTime end = earliest(aEnd, bEnd, periodEnd);
        if (!start.isBefore(end)) {
            return 0;
        }
        return Duration.between(start, end).toMinutes();
    }

    private LocalDateTime latest(LocalDateTime a, LocalDateTime b, LocalDateTime c) {
        LocalDateTime result = a.isAfter(b) ? a : b;
        return result.isAfter(c) ? result : c;
    }

    private LocalDateTime earliest(LocalDateTime a, LocalDateTime b, LocalDateTime c) {
        LocalDateTime result = a.isBefore(b) ? a : b;
        return result.isBefore(c) ? result : c;
    }

    private long overlapMinutes(LocalDateTime rangeStart,
                                LocalDateTime rangeEnd,
                                LocalDateTime periodStart,
                                LocalDateTime periodEnd) {
        LocalDateTime effectiveStart = rangeStart.isAfter(periodStart) ? rangeStart : periodStart;
        LocalDateTime effectiveEnd = rangeEnd.isBefore(periodEnd) ? rangeEnd : periodEnd;

        if (!effectiveStart.isBefore(effectiveEnd)) {
            return 0;
        }
        return Duration.between(effectiveStart, effectiveEnd).toMinutes();
    }

    private double calculateAverageResponseTime(List<MonitoringMetric> metrics) {
        return metrics.stream()
                .mapToDouble(MonitoringMetric::getResponseTime)
                .average()
                .orElse(0.0);
    }

    private double calculateAverageErrorRate(List<MonitoringMetric> metrics) {
        return metrics.stream()
                .mapToDouble(MonitoringMetric::getErrorRate)
                .average()
                .orElse(0.0);
    }

    private double calculateResponseTimeCompliance(List<MonitoringMetric> metrics, int responseTimeLimit) {
        if (metrics.isEmpty()) {
            return 100.0;
        }
        long compliantSamples = metrics.stream()
                .filter(metric -> metric.getResponseTime() <= responseTimeLimit)
                .count();
        return (compliantSamples / (double) metrics.size()) * 100.0;
    }

    private double calculateSlaScore(Sla sla,
                                     double uptimePercentage,
                                     double averageResponseTime,
                                     double averageErrorRate,
                                     List<MonitoringMetric> metrics) {
        double uptimeComponent = Math.min(100.0, (uptimePercentage / sla.getUptimeTarget()) * 100.0);

        double responseComponent = 100.0;
        if (!metrics.isEmpty() && averageResponseTime > 0) {
            responseComponent = Math.min(100.0, (sla.getResponseTimeLimit() / averageResponseTime) * 100.0);
        }

        double errorComponent = 100.0;
        if (!metrics.isEmpty() && sla.getErrorRateLimit() > 0) {
            if (averageErrorRate > sla.getErrorRateLimit()) {
                errorComponent = Math.max(
                        0.0,
                        100.0 - ((averageErrorRate - sla.getErrorRateLimit()) / sla.getErrorRateLimit() * 100.0)
                );
            }
        }

        return uptimeComponent * UPTIME_WEIGHT
                + responseComponent * RESPONSE_WEIGHT
                + errorComponent * ERROR_WEIGHT;
    }

    private SlaStatus determineStatus(Sla sla,
                                      double uptimePercentage,
                                      double averageResponseTime,
                                      double averageErrorRate,
                                      List<MonitoringMetric> metrics) {
        if (sla.getStatus() == SlaStatus.ARCHIVED) {
            return SlaStatus.ARCHIVED;
        }
        if (sla.getStatus() == SlaStatus.INACTIVE) {
            return SlaStatus.INACTIVE;
        }

        boolean uptimeBreached = uptimePercentage < sla.getUptimeTarget();
        boolean responseBreached = !metrics.isEmpty() && averageResponseTime > sla.getResponseTimeLimit();
        boolean errorBreached = !metrics.isEmpty() && averageErrorRate > sla.getErrorRateLimit();

        if (uptimeBreached || responseBreached || errorBreached) {
            return SlaStatus.BREACHED;
        }

        boolean uptimeWarning = uptimePercentage < sla.getUptimeTarget() + WARNING_UPTIME_MARGIN;
        boolean responseWarning = !metrics.isEmpty()
                && averageResponseTime > sla.getResponseTimeLimit() * WARNING_RESPONSE_RATIO;
        boolean errorWarning = !metrics.isEmpty()
                && averageErrorRate > sla.getErrorRateLimit() * WARNING_ERROR_RATIO;

        if (uptimeWarning || responseWarning || errorWarning) {
            return SlaStatus.WARNING;
        }

        return SlaStatus.ACTIVE;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
