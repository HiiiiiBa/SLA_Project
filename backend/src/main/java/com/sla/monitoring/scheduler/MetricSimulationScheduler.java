package com.sla.monitoring.scheduler;

import com.sla.monitoring.config.MetricSimulationProperties;
import com.sla.monitoring.service.MetricSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that generates simulated monitoring metrics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sla.simulation", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MetricSimulationScheduler {

    private final MetricSimulationService metricSimulationService;
    private final MetricSimulationProperties metricSimulationProperties;

    /**
     * Generates simulated metrics for all active services on a configurable schedule.
     */
    @Scheduled(cron = "${sla.simulation.cron:0 */5 * * * *}")
    public void runScheduledSimulation() {
        log.info("Scheduled metric simulation started (scenario: {})",
                metricSimulationProperties.getScenario());
        try {
            metricSimulationService.simulateAll(metricSimulationProperties.getScenario());
            log.info("Scheduled metric simulation completed successfully");
        } catch (Exception ex) {
            log.error("Scheduled metric simulation failed", ex);
        }
    }
}
