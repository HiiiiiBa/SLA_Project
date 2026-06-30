package com.sla.monitoring.dto.request;

import com.sla.monitoring.entity.enums.MetricStatus;
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
public class MonitoringMetricCreateRequest {

    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;

    @NotNull(message = "Response time is required")
    @PositiveOrZero(message = "Response time must be greater than or equal to 0")
    private Double responseTime;

    @NotNull(message = "Status is required")
    private MetricStatus status;

    @NotNull(message = "Error rate is required")
    @PositiveOrZero(message = "Error rate must be greater than or equal to 0")
    private Double errorRate;

    @NotNull(message = "Service id is required")
    private Long serviceId;

    @NotNull(message = "SLA id is required")
    private Long slaId;
}
