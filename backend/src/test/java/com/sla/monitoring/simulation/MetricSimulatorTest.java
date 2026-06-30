package com.sla.monitoring.simulation;

import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.MetricStatus;
import com.sla.monitoring.entity.enums.SlaStatus;
import com.sla.monitoring.simulation.model.SimulatedMetric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class MetricSimulatorTest {

    private MetricSimulator metricSimulator;
    private Service service;
    private Sla sla;

    @BeforeEach
    void setUp() {
        metricSimulator = new MetricSimulator();
        sla = Sla.builder()
                .id(1L)
                .name("Production API")
                .status(SlaStatus.ACTIVE)
                .uptimeTarget(99.5)
                .responseTimeLimit(500)
                .errorRateLimit(1.0)
                .build();
        service = Service.builder()
                .id(10L)
                .name("Auth API")
                .sla(sla)
                .build();
    }

    @ParameterizedTest
    @EnumSource(SimulationScenario.class)
    @DisplayName("Generates metrics with valid status, response time and error rate")
    void simulateProducesValidMetric(SimulationScenario scenario) {
        SimulatedMetric metric = metricSimulator.simulate(service, sla, scenario);

        assertThat(metric.getServiceId()).isEqualTo(10L);
        assertThat(metric.getSlaId()).isEqualTo(1L);
        assertThat(metric.getTimestamp()).isNotNull();
        assertThat(metric.getStatus()).isIn(MetricStatus.UP, MetricStatus.DOWN);
        assertThat(metric.getResponseTime()).isGreaterThanOrEqualTo(0.0);
        assertThat(metric.getErrorRate()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    @DisplayName("NORMAL scenario produces mostly UP metrics over many runs")
    void normalScenarioMostlyUp() {
        long upCount = simulateMany(SimulationScenario.NORMAL, 200).stream()
                .filter(metric -> metric.getStatus() == MetricStatus.UP)
                .count();

        assertThat(upCount).isGreaterThan(150);
    }

    @Test
    @DisplayName("OUTAGE scenario produces mostly DOWN metrics over many runs")
    void outageScenarioMostlyDown() {
        long downCount = simulateMany(SimulationScenario.OUTAGE, 200).stream()
                .filter(metric -> metric.getStatus() == MetricStatus.DOWN)
                .count();

        assertThat(downCount).isGreaterThan(120);
    }

    @Test
    @DisplayName("UP metrics stay within reasonable response time bounds")
    void upMetricsRespectResponseTimeBounds() {
        for (int i = 0; i < 50; i++) {
            SimulatedMetric metric = metricSimulator.simulate(service, sla, SimulationScenario.NORMAL);
            if (metric.getStatus() == MetricStatus.UP) {
                assertThat(metric.getResponseTime()).isLessThanOrEqualTo(sla.getResponseTimeLimit() * 0.75);
            }
        }
    }

    private java.util.List<SimulatedMetric> simulateMany(SimulationScenario scenario, int runs) {
        java.util.List<SimulatedMetric> metrics = new java.util.ArrayList<>();
        for (int i = 0; i < runs; i++) {
            metrics.add(metricSimulator.simulate(service, sla, scenario));
        }
        return metrics;
    }
}
