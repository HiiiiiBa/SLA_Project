package com.sla.monitoring.entity;

import com.sla.monitoring.entity.enums.NotificationChannel;
import com.sla.monitoring.entity.enums.NotificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_channel", columnList = "channel"),
        @Index(name = "idx_notifications_created_at", columnList = "created_at"),
        @Index(name = "idx_notifications_sla_id", columnList = "sla_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_id")
    private Long alertId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(length = 512)
    private String recipient;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "sla_id", nullable = false)
    private Long slaId;

    @Column(name = "sla_name", nullable = false)
    private String slaName;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
