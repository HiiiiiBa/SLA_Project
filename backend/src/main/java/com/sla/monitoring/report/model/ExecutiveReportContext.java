package com.sla.monitoring.report.model;

import com.sla.monitoring.dto.response.ExecutiveReportKpiSummary;
import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Project;
import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.engine.model.SlaEvaluationResult;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class ExecutiveReportContext {

    Project project;
    Sla sla;
    LocalDateTime periodStart;
    LocalDateTime periodEnd;
    SlaEvaluationResult evaluation;
    ExecutiveReportKpiSummary kpiSummary;
    List<MonitoringMetric> metrics;
    List<Incident> incidents;
    List<Alert> alerts;
    List<Service> services;
}
