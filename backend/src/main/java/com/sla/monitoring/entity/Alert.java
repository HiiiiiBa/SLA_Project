package com.sla.monitoring.entity;

import com.sla.monitoring.audit.BaseAuditEntity;
import com.sla.monitoring.entity.enums.AlertStatus;
import com.sla.monitoring.entity.enums.AlertType;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alerts_sla_id", columnList = "sla_id"),
        @Index(name = "idx_alerts_status", columnList = "status"),
        @Index(name = "idx_alerts_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AlertType type;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_id", nullable = false)
    private Sla sla;
}
