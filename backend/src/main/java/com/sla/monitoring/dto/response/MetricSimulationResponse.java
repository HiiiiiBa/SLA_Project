package com.sla.monitoring.dto.response;

import com.sla.monitoring.simulation.SimulationScenario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for a metric simulation run.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricSimulationResponse {

    private SimulationScenario scenario;
    private int servicesProcessed;
    private int metricsGenerated;
    private List<MonitoringMetricResponse> metrics;
}
