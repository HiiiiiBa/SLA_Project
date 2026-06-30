package com.sla.monitoring.report.model;

import com.sla.monitoring.engine.model.SlaEvaluationResult;
import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.Client;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Report;
import com.sla.monitoring.entity.Sla;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Aggregated data used to generate PDF and CSV exports.
 */
@Value
@Builder
public class ReportExportData {

    Report report;
    Sla sla;
    Client client;
    SlaEvaluationResult evaluation;
    List<MonitoringMetric> metrics;
    List<Incident> incidents;
    List<Alert> alerts;
}
