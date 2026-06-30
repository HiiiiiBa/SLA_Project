package com.sla.monitoring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the SLA calculation engine.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sla.engine")
public class SlaEngineProperties {

    /**
     * Enables or disables scheduled SLA evaluations.
     */
    private boolean enabled = true;

    /**
     * Cron expression for automatic SLA evaluation (default: every hour).
     */
    private String cron = "0 0 * * * *";

    /**
     * Rolling evaluation window in hours.
     */
    private int evaluationPeriodHours = 24;

    /**
     * Automatically creates alerts when SLA status changes to WARNING or BREACHED.
     */
    private boolean autoCreateAlerts = true;

    /**
     * Automatically generates a report after each evaluation cycle.
     */
    private boolean autoGenerateReports = true;
}
