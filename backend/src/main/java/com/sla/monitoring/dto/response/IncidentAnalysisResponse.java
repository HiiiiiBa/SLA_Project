package com.sla.monitoring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentAnalysisResponse {

    private String summary;
    private String probableCause;
    private String businessImpact;
    private String estimatedPriority;
    private List<String> recommendedSteps;
}
