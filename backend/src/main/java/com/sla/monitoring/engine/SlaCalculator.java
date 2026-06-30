package com.sla.monitoring.engine;

import com.sla.monitoring.entity.Incident;
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
     * Evaluates SLA compliance over the given period.
     */
    public SlaEvaluationResult evaluate(Sla sla,
                                        List<MonitoringMetric> metrics,
                                        List<Incident> incidents,
                                        LocalDateTime periodStart,
                                        LocalDateTime periodEnd) {
        double uptimePercentage = calculateUptimePercentage(metrics, incidents, periodStart, periodEnd);
        double averageResponseTime = calculateAverageResponseTime(metrics);
        double averageErrorRate = calculateAverageErrorRate(metrics);
        double responseTimeCompliance = calculateResponseTimeCompliance(metrics, sla.getResponseTimeLimit());
        double slaScore = calculateSlaScore(sla, uptimePercentage, averageResponseTime, averageErrorRate, metrics);

        SlaStatus currentStatus = determineStatus(
                sla,
                uptimePercentage,
                averageResponseTime,
                averageErrorRate,
                metrics
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
                .metricsAnalyzed(metrics.size())
                .incidentsAnalyzed(incidents.size())
                .statusChanged(sla.getStatus() != currentStatus)
                .alertCreated(false)
                .reportCreated(false)
                .build();
    }

    private double calculateUptimePercentage(List<MonitoringMetric> metrics,
                                             List<Incident> incidents,
                                             LocalDateTime periodStart,
                                             LocalDateTime periodEnd) {
        long periodMinutes = Math.max(1, Duration.between(periodStart, periodEnd).toMinutes());
        double incidentDowntimeMinutes = calculateIncidentDowntimeMinutes(incidents, periodStart, periodEnd);
        double incidentBasedUptime = Math.max(0.0, 100.0 - (incidentDowntimeMinutes / periodMinutes * 100.0));

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
                                                    LocalDateTime periodStart,
                                                    LocalDateTime periodEnd) {
        return incidents.stream()
                .mapToLong(incident -> overlapMinutes(incident, periodStart, periodEnd))
                .sum();
    }

    private long overlapMinutes(Incident incident, LocalDateTime periodStart, LocalDateTime periodEnd) {
        LocalDateTime incidentEnd = incident.getEndTime() != null ? incident.getEndTime() : periodEnd;
        LocalDateTime effectiveStart = incident.getStartTime().isAfter(periodStart)
                ? incident.getStartTime() : periodStart;
        LocalDateTime effectiveEnd = incidentEnd.isBefore(periodEnd) ? incidentEnd : periodEnd;

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
