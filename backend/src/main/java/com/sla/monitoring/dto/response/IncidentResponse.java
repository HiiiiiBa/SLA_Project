package com.sla.monitoring.dto.response;

import com.sla.monitoring.entity.enums.IncidentSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private IncidentSeverity severity;
    private String description;
    private Long slaId;
    private Long projectId;
    private String projectName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
