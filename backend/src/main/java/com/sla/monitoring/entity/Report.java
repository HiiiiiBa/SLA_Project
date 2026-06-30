package com.sla.monitoring.entity;

import com.sla.monitoring.audit.BaseAuditEntity;
import com.sla.monitoring.entity.enums.ReportFormat;
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
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_reports_sla_id", columnList = "sla_id"),
        @Index(name = "idx_reports_period", columnList = "period_start, period_end")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PositiveOrZero
    @Column(name = "sla_result", nullable = false)
    private Double slaResult;

    @NotNull
    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @NotNull
    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @NotNull
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReportFormat format;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_id", nullable = false)
    private Sla sla;
}
