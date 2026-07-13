package com.sla.monitoring.engine.model;

import com.sla.monitoring.entity.enums.SlaStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Result of an SLA evaluation for a single contract.
 */
@Value
@Builder
public class SlaEvaluationResult {

    Long slaId;
    String slaName;
    SlaStatus previousStatus;
    SlaStatus currentStatus;
    double uptimePercentage;
    double averageResponseTime;
    double averageErrorRate;
    double responseTimeCompliance;
    double slaScore;
    LocalDateTime periodStart;
    LocalDateTime periodEnd;
    int metricsAnalyzed;
    int incidentsAnalyzed;
    long maintenanceMinutesExcluded;
    boolean statusChanged;
    boolean alertCreated;
    boolean reportCreated;
}
