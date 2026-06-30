package com.sla.monitoring.notification.dto;

import com.sla.monitoring.entity.enums.AlertStatus;
import com.sla.monitoring.entity.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payload broadcast to WebSocket subscribers when a new alert is created.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertNotificationMessage {

    private Long alertId;
    private AlertType type;
    private AlertStatus status;
    private String message;
    private Long slaId;
    private String slaName;
    private String clientName;
    private LocalDateTime createdAt;
}
