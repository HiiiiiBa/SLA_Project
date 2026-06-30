package com.sla.monitoring.dto.response;

import com.sla.monitoring.entity.enums.ReportFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private Long id;
    private Double slaResult;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime generatedAt;
    private ReportFormat format;
    private Long slaId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
