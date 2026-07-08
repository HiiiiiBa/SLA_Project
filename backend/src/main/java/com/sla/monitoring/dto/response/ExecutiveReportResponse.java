package com.sla.monitoring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutiveReportResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private String clientName;
    private Long slaId;
    private String slaName;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime generatedAt;
    private String generatedByName;

    private ExecutiveReportKpiSummary kpiSummary;

    private String executiveSummary;
    private String kpiAnalysis;
    private String incidentAnalysis;
    private String performanceTrends;
    private List<String> recommendations;
    private String overallConclusion;
}
