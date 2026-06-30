package com.sla.monitoring.entity;

import com.sla.monitoring.audit.BaseAuditEntity;
import com.sla.monitoring.entity.enums.MetricStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "monitoring_metrics", indexes = {
        @Index(name = "idx_monitoring_metrics_sla_id", columnList = "sla_id"),
        @Index(name = "idx_monitoring_metrics_service_id", columnList = "service_id"),
        @Index(name = "idx_monitoring_metrics_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitoringMetric extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotNull
    @PositiveOrZero
    @Column(name = "response_time", nullable = false)
    private Double responseTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MetricStatus status;

    @NotNull
    @PositiveOrZero
    @Column(name = "error_rate", nullable = false)
    private Double errorRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_id", nullable = false)
    private Sla sla;
}
