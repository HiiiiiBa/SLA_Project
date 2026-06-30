package com.sla.monitoring.entity;

import com.sla.monitoring.audit.BaseAuditEntity;
import com.sla.monitoring.entity.enums.SlaStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "slas", indexes = {
        @Index(name = "idx_slas_client_id", columnList = "client_id"),
        @Index(name = "idx_slas_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sla extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlaStatus status;

    @NotNull
    @Positive
    @Column(name = "uptime_target", nullable = false)
    private Double uptimeTarget;

    @NotNull
    @Positive
    @Column(name = "response_time_limit", nullable = false)
    private Integer responseTimeLimit;

    @NotNull
    @Positive
    @Column(name = "error_rate_limit", nullable = false)
    private Double errorRateLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToMany(mappedBy = "sla", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Service> services = new ArrayList<>();

    @OneToMany(mappedBy = "sla", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MonitoringMetric> metrics = new ArrayList<>();

    @OneToMany(mappedBy = "sla", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Incident> incidents = new ArrayList<>();

    @OneToMany(mappedBy = "sla", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Alert> alerts = new ArrayList<>();

    @OneToMany(mappedBy = "sla", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Report> reports = new ArrayList<>();
}
