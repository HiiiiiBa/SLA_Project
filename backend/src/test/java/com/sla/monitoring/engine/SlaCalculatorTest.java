package com.sla.monitoring.engine;

import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.MaintenanceWindow;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.IncidentSeverity;
import com.sla.monitoring.entity.enums.MaintenanceWindowStatus;
import com.sla.monitoring.entity.enums.MetricStatus;
import com.sla.monitoring.entity.enums.SlaStatus;
import com.sla.monitoring.engine.model.SlaEvaluationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlaCalculatorTest {

    private SlaCalculator slaCalculator;
    private Sla sla;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    @BeforeEach
    void setUp() {
        slaCalculator = new SlaCalculator();
        periodEnd = LocalDateTime.of(2026, 6, 30, 12, 0);
        periodStart = periodEnd.minusHours(24);

        sla = Sla.builder()
                .id(1L)
                .name("Production API")
                .status(SlaStatus.ACTIVE)
                .uptimeTarget(99.5)
                .responseTimeLimit(500)
                .errorRateLimit(1.0)
                .build();
    }

    @Test
    @DisplayName("Returns ACTIVE when all metrics are within SLA targets")
    void evaluateReturnsActiveWhenCompliant() {
        List<MonitoringMetric> metrics = List.of(
                metric(100.0, 0.1, MetricStatus.UP),
                metric(120.0, 0.2, MetricStatus.UP),
                metric(200.0, 0.4, MetricStatus.UP)
        );

        SlaEvaluationResult result = slaCalculator.evaluate(sla, metrics, List.of(), periodStart, periodEnd);

        assertThat(result.getCurrentStatus()).isEqualTo(SlaStatus.ACTIVE);
        assertThat(result.getUptimePercentage()).isEqualTo(100.0);
        assertThat(result.getSlaScore()).isGreaterThan(95.0);
        assertThat(result.getMaintenanceMinutesExcluded()).isZero();
    }

    @Test
    @DisplayName("Returns BREACHED when uptime falls below target")
    void evaluateReturnsBreachedWhenUptimeTooLow() {
        List<MonitoringMetric> metrics = List.of(
                metric(100.0, 0.1, MetricStatus.UP),
                metric(100.0, 0.1, MetricStatus.DOWN),
                metric(100.0, 0.1, MetricStatus.DOWN),
                metric(100.0, 0.1, MetricStatus.DOWN)
        );

        SlaEvaluationResult result = slaCalculator.evaluate(sla, metrics, List.of(), periodStart, periodEnd);

        assertThat(result.getCurrentStatus()).isEqualTo(SlaStatus.BREACHED);
        assertThat(result.getUptimePercentage()).isEqualTo(25.0);
    }

    @Test
    @DisplayName("Returns BREACHED when average response time exceeds limit")
    void evaluateReturnsBreachedWhenResponseTimeTooHigh() {
        List<MonitoringMetric> metrics = List.of(
                metric(800.0, 0.1, MetricStatus.UP),
                metric(900.0, 0.2, MetricStatus.UP)
        );

        SlaEvaluationResult result = slaCalculator.evaluate(sla, metrics, List.of(), periodStart, periodEnd);

        assertThat(result.getCurrentStatus()).isEqualTo(SlaStatus.BREACHED);
        assertThat(result.getAverageResponseTime()).isEqualTo(850.0);
    }

    @Test
    @DisplayName("Returns WARNING when metrics are close to SLA limits")
    void evaluateReturnsWarningWhenNearLimits() {
        sla.setUptimeTarget(99.0);
        List<MonitoringMetric> metrics = List.of(
                metric(460.0, 0.95, MetricStatus.UP),
                metric(470.0, 0.92, MetricStatus.UP)
        );

        SlaEvaluationResult result = slaCalculator.evaluate(sla, metrics, List.of(), periodStart, periodEnd);

        assertThat(result.getCurrentStatus()).isEqualTo(SlaStatus.WARNING);
    }

    @Test
    @DisplayName("Incident downtime reduces uptime percentage")
    void evaluateUsesIncidentDowntime() {
        Incident incident = Incident.builder()
                .startTime(periodStart.plusHours(1))
                .endTime(periodStart.plusHours(7))
                .severity(IncidentSeverity.HIGH)
                .description("Outage")
                .build();

        SlaEvaluationResult result = slaCalculator.evaluate(sla, List.of(), List.of(incident), periodStart, periodEnd);

        assertThat(result.getUptimePercentage()).isEqualTo(75.0);
        assertThat(result.getCurrentStatus()).isEqualTo(SlaStatus.BREACHED);
    }

    @Test
    @DisplayName("Incident entirely inside maintenance does not reduce uptime")
    void evaluateIgnoresIncidentInsideMaintenance() {
        Incident incident = Incident.builder()
                .startTime(periodStart.plusHours(2))
                .endTime(periodStart.plusHours(4))
                .severity(IncidentSeverity.HIGH)
                .description("Planned outage")
                .build();

        MaintenanceWindow window = MaintenanceWindow.builder()
                .title("Nightly upgrade")
                .startTime(periodStart.plusHours(1))
                .endTime(periodStart.plusHours(5))
                .status(MaintenanceWindowStatus.SCHEDULED)
                .build();

        SlaEvaluationResult result = slaCalculator.evaluate(
                sla, List.of(), List.of(incident), List.of(window), periodStart, periodEnd);

        assertThat(result.getUptimePercentage()).isEqualTo(100.0);
        assertThat(result.getMaintenanceMinutesExcluded()).isEqualTo(240L);
        assertThat(result.getCurrentStatus()).isEqualTo(SlaStatus.ACTIVE);
    }

    @Test
    @DisplayName("DOWN metric inside maintenance is ignored")
    void evaluateIgnoresDownMetricInsideMaintenance() {
        List<MonitoringMetric> metrics = List.of(
                metricAt(100.0, 0.1, MetricStatus.UP, periodStart.plusHours(1)),
                metricAt(100.0, 0.1, MetricStatus.DOWN, periodStart.plusHours(3)),
                metricAt(100.0, 0.1, MetricStatus.UP, periodStart.plusHours(6))
        );

        MaintenanceWindow window = MaintenanceWindow.builder()
                .title("Patch window")
                .startTime(periodStart.plusHours(2))
                .endTime(periodStart.plusHours(4))
                .status(MaintenanceWindowStatus.ACTIVE)
                .build();

        SlaEvaluationResult result = slaCalculator.evaluate(
                sla, metrics, List.of(), List.of(window), periodStart, periodEnd);

        assertThat(result.getMetricsAnalyzed()).isEqualTo(2);
        assertThat(result.getUptimePercentage()).isEqualTo(100.0);
        assertThat(result.getCurrentStatus()).isEqualTo(SlaStatus.ACTIVE);
        assertThat(result.getMaintenanceMinutesExcluded()).isEqualTo(120L);
    }

    @Test
    @DisplayName("Downtime outside maintenance still reduces uptime")
    void evaluateKeepsDowntimeOutsideMaintenance() {
        Incident incident = Incident.builder()
                .startTime(periodStart.plusHours(1))
                .endTime(periodStart.plusHours(7))
                .severity(IncidentSeverity.HIGH)
                .description("Partial overlap")
                .build();

        MaintenanceWindow window = MaintenanceWindow.builder()
                .title("Partial window")
                .startTime(periodStart.plusHours(1))
                .endTime(periodStart.plusHours(3))
                .status(MaintenanceWindowStatus.SCHEDULED)
                .build();

        SlaEvaluationResult result = slaCalculator.evaluate(
                sla, List.of(), List.of(incident), List.of(window), periodStart, periodEnd);

        assertThat(result.getMaintenanceMinutesExcluded()).isEqualTo(120L);
        assertThat(result.getUptimePercentage()).isEqualTo(81.82);
        assertThat(result.getCurrentStatus()).isEqualTo(SlaStatus.BREACHED);
    }

    private MonitoringMetric metric(double responseTime, double errorRate, MetricStatus status) {
        return metricAt(responseTime, errorRate, status, periodStart.plusHours(1));
    }

    private MonitoringMetric metricAt(double responseTime,
                                      double errorRate,
                                      MetricStatus status,
                                      LocalDateTime timestamp) {
        return MonitoringMetric.builder()
                .responseTime(responseTime)
                .errorRate(errorRate)
                .status(status)
                .timestamp(timestamp)
                .build();
    }
}
