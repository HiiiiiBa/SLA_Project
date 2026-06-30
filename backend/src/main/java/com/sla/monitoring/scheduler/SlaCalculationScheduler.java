package com.sla.monitoring.scheduler;

import com.sla.monitoring.config.SlaEngineProperties;
import com.sla.monitoring.service.SlaEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that triggers automatic SLA evaluation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sla.engine", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SlaCalculationScheduler {

    private final SlaEngineService slaEngineService;
    private final SlaEngineProperties slaEngineProperties;

    /**
     * Runs SLA evaluation for all active contracts on a configurable cron schedule.
     */
    @Scheduled(cron = "${sla.engine.cron:0 0 * * * *}")
    public void runScheduledEvaluation() {
        log.info("Scheduled SLA evaluation started (window: {} hour(s))",
                slaEngineProperties.getEvaluationPeriodHours());
        try {
            slaEngineService.evaluateAll();
            log.info("Scheduled SLA evaluation completed successfully");
        } catch (Exception ex) {
            log.error("Scheduled SLA evaluation failed", ex);
        }
    }
}
