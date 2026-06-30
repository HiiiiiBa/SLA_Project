package com.sla.monitoring.service;

import com.sla.monitoring.dto.response.MetricSimulationResponse;
import com.sla.monitoring.simulation.SimulationScenario;

/**
 * Generates and persists simulated monitoring metrics.
 */
public interface MetricSimulationService {

    /**
     * Simulates metrics for all services linked to active SLAs.
     */
    MetricSimulationResponse simulateAll(SimulationScenario scenario);

    /**
     * Simulates metrics for all services of a given SLA.
     */
    MetricSimulationResponse simulateForSla(Long slaId, SimulationScenario scenario);

    /**
     * Simulates a single metric for one service.
     */
    MetricSimulationResponse simulateForService(Long serviceId, SimulationScenario scenario);
}
