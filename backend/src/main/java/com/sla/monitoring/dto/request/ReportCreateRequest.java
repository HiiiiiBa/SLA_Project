package com.sla.monitoring.dto.request;

import com.sla.monitoring.entity.enums.ReportFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateRequest {

    @NotNull(message = "SLA result is required")
    @PositiveOrZero(message = "SLA result must be greater than or equal to 0")
    private Double slaResult;

    @NotNull(message = "Period start is required")
    private LocalDateTime periodStart;

    @NotNull(message = "Period end is required")
    private LocalDateTime periodEnd;

    @NotNull(message = "Format is required")
    private ReportFormat format;

    @NotNull(message = "SLA id is required")
    private Long slaId;
}
