package com.sla.monitoring.dto.response;

import com.sla.monitoring.entity.enums.NotificationChannel;
import com.sla.monitoring.entity.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long alertId;
    private NotificationChannel channel;
    private NotificationStatus status;
    private String recipient;
    private String message;
    private Long slaId;
    private String slaName;
    private String clientName;
    private LocalDateTime createdAt;
}
