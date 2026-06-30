package com.sla.monitoring.simulation.model;

import com.sla.monitoring.entity.enums.MetricStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Generated metric values before persistence.
 */
@Value
@Builder
public class SimulatedMetric {

    Long serviceId;
    Long slaId;
    LocalDateTime timestamp;
    MetricStatus status;
    double responseTime;
    double errorRate;
}
