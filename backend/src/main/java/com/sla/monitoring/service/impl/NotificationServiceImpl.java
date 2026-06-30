package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.response.NotificationResponse;
import com.sla.monitoring.entity.Notification;
import com.sla.monitoring.entity.enums.NotificationChannel;
import com.sla.monitoring.entity.enums.NotificationStatus;
import com.sla.monitoring.repository.NotificationRepository;
import com.sla.monitoring.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<NotificationResponse> findAll(NotificationChannel channel) {
        List<Notification> notifications = channel == null
                ? notificationRepository.findAllByOrderByCreatedAtDesc()
                : notificationRepository.findByChannelOrderByCreatedAtDesc(channel);
        return notifications.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void record(Long alertId,
                       NotificationChannel channel,
                       NotificationStatus status,
                       String recipient,
                       String message,
                       Long slaId,
                       String slaName,
                       String clientName) {
        Notification notification = Notification.builder()
                .alertId(alertId)
                .channel(channel)
                .status(status)
                .recipient(recipient)
                .message(message)
                .slaId(slaId)
                .slaName(slaName)
                .clientName(clientName)
                .build();
        notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .alertId(notification.getAlertId())
                .channel(notification.getChannel())
                .status(notification.getStatus())
                .recipient(notification.getRecipient())
                .message(notification.getMessage())
                .slaId(notification.getSlaId())
                .slaName(notification.getSlaName())
                .clientName(notification.getClientName())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
