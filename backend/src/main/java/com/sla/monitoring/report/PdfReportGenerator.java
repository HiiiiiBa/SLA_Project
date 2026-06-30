package com.sla.monitoring.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Report;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.engine.model.SlaEvaluationResult;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.report.model.ReportExportData;
import com.sla.monitoring.report.model.ReportExportResult;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates SLA report PDF documents using OpenPDF.
 */
@Component
public class PdfReportGenerator implements ReportGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

    @Override
    public ReportExportResult generate(ReportExportData data) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addTitle(document, data);
            addGlobalPerformanceSection(document, data);
            addSummarySection(document, data);
            addMetricsSection(document, data.getMetrics());
            addIncidentsSection(document, data.getIncidents());
            addAlertsSection(document, data.getAlerts());

            document.close();

            String filename = buildFilename(data.getReport().getId(), "pdf");
            return new ReportExportResult(outputStream.toByteArray(), filename, "application/pdf");
        } catch (DocumentException ex) {
            throw new BusinessException("Failed to generate PDF report: " + ex.getMessage());
        } catch (Exception ex) {
            throw new BusinessException("Failed to generate PDF report: " + ex.getMessage());
        }
    }

    private void addTitle(Document document, ReportExportData data) throws DocumentException {
        Paragraph title = new Paragraph("SLA Monitoring Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(12);
        document.add(title);

        Report report = data.getReport();
        document.add(new Paragraph(
                "Report #" + report.getId()
                        + " | Generated: " + formatDateTime(report.getGeneratedAt()),
                NORMAL_FONT));
        document.add(new Paragraph(" ", NORMAL_FONT));
    }

    private void addGlobalPerformanceSection(Document document, ReportExportData data) throws DocumentException {
        document.add(new Paragraph("Performance globale", SECTION_FONT));

        SlaEvaluationResult evaluation = data.getEvaluation();
        Sla sla = data.getSla();

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.setSpacingAfter(16);

        addRow(table, "Statut SLA", evaluation.getCurrentStatus().name());
        addRow(table, "Score global", formatNumber(evaluation.getSlaScore()) + " / 100");
        addRow(table, "Uptime réalisé", formatNumber(evaluation.getUptimePercentage()) + "% (cible "
                + formatNumber(sla.getUptimeTarget()) + "%)");
        addRow(table, "Conformité temps réponse", formatNumber(evaluation.getResponseTimeCompliance()) + "%");
        addRow(table, "Taux d'erreur moyen", formatNumber(evaluation.getAverageErrorRate()) + "%");
        addRow(table, "Incidents sur la période", String.valueOf(data.getIncidents().size()));
        addRow(table, "Alertes sur la période", String.valueOf(data.getAlerts().size()));
        addRow(table, "Verdict", globalVerdict(evaluation));

        document.add(table);
    }

    private String globalVerdict(SlaEvaluationResult evaluation) {
        return switch (evaluation.getCurrentStatus()) {
            case ACTIVE -> "Conforme — objectifs SLA respectés";
            case WARNING -> "Attention — dérives détectées, surveillance requise";
            case BREACHED -> "Non conforme — SLA violé";
            case INACTIVE -> "Inactif — hors périmètre de monitoring";
            case ARCHIVED -> "Archivé — contrat clos";
        };
    }

    private void addAlertsSection(Document document, List<Alert> alerts) throws DocumentException {
        document.add(new Paragraph("Alertes", SECTION_FONT));

        if (alerts.isEmpty()) {
            document.add(new Paragraph("Aucune alerte sur cette période.", NORMAL_FONT));
            return;
        }

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);

        addHeader(table, "Date");
        addHeader(table, "Type");
        addHeader(table, "Statut");
        addHeader(table, "Message");

        for (Alert alert : alerts) {
            addCell(table, formatDateTime(alert.getCreatedAt()));
            addCell(table, alert.getType().name());
            addCell(table, alert.getStatus().name());
            addCell(table, alert.getMessage());
        }

        document.add(table);
    }

    private void addSummarySection(Document document, ReportExportData data) throws DocumentException {
        document.add(new Paragraph("Détails du contrat", SECTION_FONT));

        Sla sla = data.getSla();
        SlaEvaluationResult evaluation = data.getEvaluation();

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.setSpacingAfter(16);

        addRow(table, "Client", data.getClient().getName());
        addRow(table, "Client Email", data.getClient().getEmail());
        addRow(table, "Project", nullToDash(data.getClient().getProjectName()));
        addRow(table, "SLA", sla.getName());
        addRow(table, "SLA Status", sla.getStatus().name());
        addRow(table, "Period Start", formatDateTime(reportPeriodStart(data)));
        addRow(table, "Period End", formatDateTime(reportPeriodEnd(data)));
        addRow(table, "Uptime Target (%)", formatNumber(sla.getUptimeTarget()));
        addRow(table, "Response Time Limit (ms)", String.valueOf(sla.getResponseTimeLimit()));
        addRow(table, "Error Rate Limit (%)", formatNumber(sla.getErrorRateLimit()));
        addRow(table, "Uptime Achieved (%)", formatNumber(evaluation.getUptimePercentage()));
        addRow(table, "Avg Response Time (ms)", formatNumber(evaluation.getAverageResponseTime()));
        addRow(table, "Avg Error Rate (%)", formatNumber(evaluation.getAverageErrorRate()));
        addRow(table, "Response Time Compliance (%)", formatNumber(evaluation.getResponseTimeCompliance()));
        addRow(table, "SLA Score", formatNumber(evaluation.getSlaScore()));
        addRow(table, "Stored SLA Result", formatNumber(data.getReport().getSlaResult()));
        addRow(table, "Metrics Analyzed", String.valueOf(evaluation.getMetricsAnalyzed()));
        addRow(table, "Incidents Analyzed", String.valueOf(evaluation.getIncidentsAnalyzed()));

        document.add(table);
    }

    private void addMetricsSection(Document document, List<MonitoringMetric> metrics) throws DocumentException {
        document.add(new Paragraph("Monitoring Metrics", SECTION_FONT));

        if (metrics.isEmpty()) {
            document.add(new Paragraph("No metrics recorded for this period.", NORMAL_FONT));
            return;
        }

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.setSpacingAfter(16);

        addHeader(table, "Timestamp");
        addHeader(table, "Service");
        addHeader(table, "Status");
        addHeader(table, "Response (ms)");
        addHeader(table, "Error (%)");

        int limit = Math.min(metrics.size(), 50);
        for (int i = 0; i < limit; i++) {
            MonitoringMetric metric = metrics.get(i);
            addCell(table, formatDateTime(metric.getTimestamp()));
            addCell(table, metric.getService().getName());
            addCell(table, metric.getStatus().name());
            addCell(table, formatNumber(metric.getResponseTime()));
            addCell(table, formatNumber(metric.getErrorRate()));
        }

        document.add(table);

        if (metrics.size() > limit) {
            document.add(new Paragraph(
                    "Showing first " + limit + " of " + metrics.size() + " metrics. Export CSV for full data.",
                    NORMAL_FONT));
        }
    }

    private void addIncidentsSection(Document document, List<Incident> incidents) throws DocumentException {
        document.add(new Paragraph("Incidents", SECTION_FONT));

        if (incidents.isEmpty()) {
            document.add(new Paragraph("No incidents recorded for this period.", NORMAL_FONT));
            return;
        }

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);

        addHeader(table, "Start");
        addHeader(table, "End");
        addHeader(table, "Severity");
        addHeader(table, "Description");

        for (Incident incident : incidents) {
            addCell(table, formatDateTime(incident.getStartTime()));
            addCell(table, incident.getEndTime() != null ? formatDateTime(incident.getEndTime()) : "Ongoing");
            addCell(table, incident.getSeverity().name());
            addCell(table, incident.getDescription());
        }

        document.add(table);
    }

    private void addRow(PdfPTable table, String label, String value) {
        table.addCell(headerCell(label));
        table.addCell(valueCell(value));
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new Color(240, 240, 240));
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setPadding(6);
        return cell;
    }

    private void addHeader(PdfPTable table, String text) {
        table.addCell(headerCell(text));
    }

    private void addCell(PdfPTable table, String text) {
        table.addCell(valueCell(text));
    }

    private String buildFilename(Long reportId, String extension) {
        return "sla-report-" + reportId + "." + extension;
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "-";
    }

    private String formatNumber(double value) {
        return String.format("%.2f", value);
    }

    private String nullToDash(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }

    private java.time.LocalDateTime reportPeriodStart(ReportExportData data) {
        return data.getReport().getPeriodStart();
    }

    private java.time.LocalDateTime reportPeriodEnd(ReportExportData data) {
        return data.getReport().getPeriodEnd();
    }
}
