package com.sla.monitoring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutiveReportListItemResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private String clientName;
    private Long slaId;
    private String slaName;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime generatedAt;
    private Double slaScore;
    private String slaStatus;
    private Integer incidentCount;
    private Integer alertCount;
    private String generatedByName;
}
