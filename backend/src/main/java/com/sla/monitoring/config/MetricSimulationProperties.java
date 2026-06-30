package com.sla.monitoring.config;

import com.sla.monitoring.simulation.SimulationScenario;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for monitoring metric simulation.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sla.simulation")
public class MetricSimulationProperties {

    /**
     * Enables or disables scheduled metric simulation.
     */
    private boolean enabled = true;

    /**
     * Cron expression for automatic simulation (default: every 5 minutes).
     */
    private String cron = "0 */5 * * * *";

    /**
     * Default scenario applied by the scheduler.
     */
    private SimulationScenario scenario = SimulationScenario.NORMAL;

    /**
     * Synchronizes monitored service status (UP/DOWN) with the simulated metric.
     */
    private boolean syncServiceStatus = true;
}
