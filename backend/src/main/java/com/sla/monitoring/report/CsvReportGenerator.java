package com.sla.monitoring.report;

import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Report;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.engine.model.SlaEvaluationResult;
import com.sla.monitoring.report.model.ReportExportData;
import com.sla.monitoring.report.model.ReportExportResult;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates SLA report CSV exports.
 */
@Component
public class CsvReportGenerator implements ReportGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ReportExportResult generate(ReportExportData data) {
        List<String> lines = new ArrayList<>();

        appendSummary(lines, data);
        lines.add("");
        appendGlobalPerformance(lines, data);
        lines.add("");
        appendMetrics(lines, data.getMetrics());
        lines.add("");
        appendIncidents(lines, data.getIncidents());
        lines.add("");
        appendAlerts(lines, data.getAlerts());

        byte[] content = String.join(System.lineSeparator(), lines).getBytes(StandardCharsets.UTF_8);
        String filename = "sla-report-" + data.getReport().getId() + ".csv";
        return new ReportExportResult(content, filename, "text/csv");
    }

    private void appendSummary(List<String> lines, ReportExportData data) {
        Report report = data.getReport();
        Sla sla = data.getSla();
        SlaEvaluationResult evaluation = data.getEvaluation();

        lines.add("section,key,value");
        lines.add(row("summary", "report_id", report.getId()));
        lines.add(row("summary", "generated_at", formatDateTime(report.getGeneratedAt())));
        lines.add(row("summary", "period_start", formatDateTime(report.getPeriodStart())));
        lines.add(row("summary", "period_end", formatDateTime(report.getPeriodEnd())));
        lines.add(row("summary", "client_name", data.getClient().getName()));
        lines.add(row("summary", "client_email", data.getClient().getEmail()));
        lines.add(row("summary", "project_name", data.getClient().getProjectName()));
        lines.add(row("summary", "sla_name", sla.getName()));
        lines.add(row("summary", "sla_status", sla.getStatus()));
        lines.add(row("summary", "uptime_target", sla.getUptimeTarget()));
        lines.add(row("summary", "response_time_limit", sla.getResponseTimeLimit()));
        lines.add(row("summary", "error_rate_limit", sla.getErrorRateLimit()));
        lines.add(row("summary", "uptime_percentage", evaluation.getUptimePercentage()));
        lines.add(row("summary", "average_response_time", evaluation.getAverageResponseTime()));
        lines.add(row("summary", "average_error_rate", evaluation.getAverageErrorRate()));
        lines.add(row("summary", "response_time_compliance", evaluation.getResponseTimeCompliance()));
        lines.add(row("summary", "sla_score", evaluation.getSlaScore()));
        lines.add(row("summary", "stored_sla_result", report.getSlaResult()));
        lines.add(row("summary", "metrics_analyzed", evaluation.getMetricsAnalyzed()));
        lines.add(row("summary", "incidents_analyzed", evaluation.getIncidentsAnalyzed()));
        lines.add(row("summary", "alerts_count", data.getAlerts().size()));
    }

    private void appendGlobalPerformance(List<String> lines, ReportExportData data) {
        SlaEvaluationResult evaluation = data.getEvaluation();
        lines.add("section,key,value");
        lines.add(row("performance", "sla_status", evaluation.getCurrentStatus()));
        lines.add(row("performance", "global_score", evaluation.getSlaScore()));
        lines.add(row("performance", "uptime_percentage", evaluation.getUptimePercentage()));
        lines.add(row("performance", "response_time_compliance", evaluation.getResponseTimeCompliance()));
        lines.add(row("performance", "average_error_rate", evaluation.getAverageErrorRate()));
        lines.add(row("performance", "incidents_count", data.getIncidents().size()));
        lines.add(row("performance", "alerts_count", data.getAlerts().size()));
        lines.add(row("performance", "verdict", globalVerdict(evaluation)));
    }

    private String globalVerdict(SlaEvaluationResult evaluation) {
        return switch (evaluation.getCurrentStatus()) {
            case ACTIVE -> "CONFORME";
            case WARNING -> "ATTENTION";
            case BREACHED -> "NON_CONFORME";
            case INACTIVE -> "INACTIF";
            case ARCHIVED -> "ARCHIVE";
        };
    }

    private void appendAlerts(List<String> lines, List<Alert> alerts) {
        lines.add("created_at,type,status,message");
        for (Alert alert : alerts) {
            lines.add(String.join(",",
                    escape(formatDateTime(alert.getCreatedAt())),
                    escape(alert.getType().name()),
                    escape(alert.getStatus().name()),
                    escape(alert.getMessage())
            ));
        }
    }

    private void appendMetrics(List<String> lines, List<MonitoringMetric> metrics) {
        lines.add("timestamp,service,status,response_time_ms,error_rate_percent");
        for (MonitoringMetric metric : metrics) {
            lines.add(String.join(",",
                    escape(formatDateTime(metric.getTimestamp())),
                    escape(metric.getService().getName()),
                    escape(metric.getStatus().name()),
                    escape(metric.getResponseTime()),
                    escape(metric.getErrorRate())
            ));
        }
    }

    private void appendIncidents(List<String> lines, List<Incident> incidents) {
        lines.add("start_time,end_time,severity,description");
        for (Incident incident : incidents) {
            lines.add(String.join(",",
                    escape(formatDateTime(incident.getStartTime())),
                    escape(incident.getEndTime() != null ? formatDateTime(incident.getEndTime()) : "Ongoing"),
                    escape(incident.getSeverity().name()),
                    escape(incident.getDescription())
            ));
        }
    }

    private String row(String section, String key, Object value) {
        return String.join(",", escape(section), escape(key), escape(value));
    }

    private String escape(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String text = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + text + "\"";
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }
}
