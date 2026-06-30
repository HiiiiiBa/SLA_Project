package com.sla.monitoring.service;

import com.sla.monitoring.dto.response.NotificationResponse;
import com.sla.monitoring.entity.enums.NotificationChannel;
import com.sla.monitoring.entity.enums.NotificationStatus;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> findAll(NotificationChannel channel);

    void record(Long alertId,
                NotificationChannel channel,
                NotificationStatus status,
                String recipient,
                String message,
                Long slaId,
                String slaName,
                String clientName);
}
