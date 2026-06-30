package com.sla.monitoring.dto.response;

import com.sla.monitoring.entity.enums.SlaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for SLA engine evaluation results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaEvaluationResponse {

    private Long slaId;
    private String slaName;
    private SlaStatus previousStatus;
    private SlaStatus currentStatus;
    private double uptimePercentage;
    private double averageResponseTime;
    private double averageErrorRate;
    private double responseTimeCompliance;
    private double slaScore;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private int metricsAnalyzed;
    private int incidentsAnalyzed;
    private boolean statusChanged;
    private boolean alertCreated;
    private boolean reportCreated;
}
