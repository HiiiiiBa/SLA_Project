package com.sla.monitoring.dto.response;

import com.sla.monitoring.entity.enums.MaintenanceWindowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceWindowResponse {

    private Long id;
    private String title;
    private String reason;
    private Long slaId;
    private String slaName;
    private Long serviceId;
    private String serviceName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private MaintenanceWindowStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
