package com.sla.monitoring.dto.response;

import com.sla.monitoring.entity.enums.AlertStatus;
import com.sla.monitoring.entity.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private Long id;
    private AlertType type;
    private String message;
    private AlertStatus status;
    private Long slaId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
