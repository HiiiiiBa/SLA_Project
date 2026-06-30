package com.sla.monitoring.simulation;

import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.MetricStatus;
import com.sla.monitoring.simulation.model.SimulatedMetric;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates realistic monitoring metrics (UP/DOWN, response time, error rate).
 */
@Component
public class MetricSimulator {

    /**
     * Builds a simulated metric for the given service and SLA contract.
     */
    public SimulatedMetric simulate(Service service, Sla sla, SimulationScenario scenario) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        boolean isUp = random.nextDouble() >= downProbability(scenario);

        MetricStatus status = isUp ? MetricStatus.UP : MetricStatus.DOWN;
        double responseTime = generateResponseTime(sla, scenario, isUp, random);
        double errorRate = generateErrorRate(sla, scenario, isUp, random);

        return SimulatedMetric.builder()
                .serviceId(service.getId())
                .slaId(sla.getId())
                .timestamp(LocalDateTime.now())
                .status(status)
                .responseTime(round(responseTime))
                .errorRate(round(errorRate))
                .build();
    }

    private double downProbability(SimulationScenario scenario) {
        return switch (scenario) {
            case NORMAL -> 0.05;
            case DEGRADED -> 0.15;
            case OUTAGE -> 0.70;
        };
    }

    private double generateResponseTime(Sla sla,
                                        SimulationScenario scenario,
                                        boolean isUp,
                                        ThreadLocalRandom random) {
        int limit = sla.getResponseTimeLimit();

        if (!isUp) {
            return limit + random.nextDouble(50.0, limit * 0.5 + 50.0);
        }

        double minRatio = switch (scenario) {
            case NORMAL -> 0.20;
            case DEGRADED -> 0.55;
            case OUTAGE -> 0.75;
        };
        double maxRatio = switch (scenario) {
            case NORMAL -> 0.70;
            case DEGRADED -> 0.95;
            case OUTAGE -> 1.20;
        };

        return random.nextDouble(limit * minRatio, limit * maxRatio);
    }

    private double generateErrorRate(Sla sla,
                                     SimulationScenario scenario,
                                     boolean isUp,
                                     ThreadLocalRandom random) {
        double limit = sla.getErrorRateLimit();

        if (!isUp) {
            return random.nextDouble(Math.max(limit, 1.0), Math.max(limit * 2.5, 2.0));
        }

        double maxRatio = switch (scenario) {
            case NORMAL -> 0.50;
            case DEGRADED -> 0.90;
            case OUTAGE -> 1.10;
        };

        return random.nextDouble(0.0, Math.max(limit * maxRatio, 0.1));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
