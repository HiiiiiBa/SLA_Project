package com.sla.monitoring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutiveReportKpiSummary {

    private Double slaScore;
    private String slaStatus;
    private Double uptimePercentage;
    private Double uptimeTarget;
    private Double averageResponseTime;
    private Double responseTimeLimit;
    private Double responseTimeCompliance;
    private Double averageErrorRate;
    private Double errorRateLimit;
    private Integer incidentCount;
    private Integer criticalIncidentCount;
    private Integer alertCount;
    private Integer servicesDown;
    private Integer servicesDegraded;
    private Integer metricsAnalyzed;
}
