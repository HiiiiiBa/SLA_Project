package com.sla.monitoring.entity;

import com.sla.monitoring.audit.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "executive_reports", indexes = {
        @Index(name = "idx_executive_reports_project_id", columnList = "project_id"),
        @Index(name = "idx_executive_reports_sla_id", columnList = "sla_id"),
        @Index(name = "idx_executive_reports_generated_at", columnList = "generated_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutiveReport extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sla_id", nullable = false)
    private Sla sla;

    @NotBlank
    @Column(name = "project_name", nullable = false)
    private String projectName;

    @NotBlank
    @Column(name = "client_name", nullable = false)
    private String clientName;

    @NotBlank
    @Column(name = "sla_name", nullable = false)
    private String slaName;

    @NotNull
    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @NotNull
    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @NotNull
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by_id")
    private User generatedBy;

    @NotBlank
    @Column(name = "kpi_summary", nullable = false, columnDefinition = "TEXT")
    private String kpiSummary;

    @NotBlank
    @Column(name = "narrative", nullable = false, columnDefinition = "TEXT")
    private String narrative;
}
