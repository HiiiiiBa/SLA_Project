package com.sla.monitoring.dto.response;

import com.sla.monitoring.entity.enums.MetricStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringMetricResponse {

    private Long id;
    private LocalDateTime timestamp;
    private Double responseTime;
    private MetricStatus status;
    private Double errorRate;
    private Long serviceId;
    private Long slaId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
