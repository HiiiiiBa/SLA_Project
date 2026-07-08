package com.sla.monitoring.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.sla.monitoring.dto.response.ExecutiveReportKpiSummary;
import com.sla.monitoring.dto.response.ExecutiveReportResponse;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.report.model.ReportExportResult;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates AI Executive Report PDF documents using OpenPDF.
 */
@Component
public class ExecutivePdfReportGenerator {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8);

    public ReportExportResult generate(ExecutiveReportResponse report) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            addTitle(document, report);
            addKpiTable(document, report.getKpiSummary());
            addCharts(document, writer, report.getKpiSummary());
            addSection(document, "1. Executive Summary", report.getExecutiveSummary());
            addSection(document, "2. KPI Summary", report.getKpiAnalysis());
            addSection(document, "3. Incident Analysis", report.getIncidentAnalysis());
            addSection(document, "4. Performance Trends", report.getPerformanceTrends());
            addRecommendations(document, report.getRecommendations());
            addSection(document, "6. Overall Conclusion", report.getOverallConclusion());

            document.close();

            String filename = report.getId() != null
                    ? "ai-executive-report-%d.pdf".formatted(report.getId())
                    : "ai-executive-report-project-%d.pdf".formatted(report.getProjectId());
            return new ReportExportResult(outputStream.toByteArray(), filename, "application/pdf");
        } catch (DocumentException ex) {
            throw new BusinessException("Failed to generate executive PDF: " + ex.getMessage());
        } catch (Exception ex) {
            throw new BusinessException("Failed to generate executive PDF: " + ex.getMessage());
        }
    }

    private void addTitle(Document document, ExecutiveReportResponse report) throws DocumentException {
        Paragraph title = new Paragraph("AI Executive Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(12);
        document.add(title);

        if (report.getId() != null) {
            document.add(new Paragraph("Rapport #" + report.getId(), NORMAL_FONT));
        }
        document.add(new Paragraph("Projet : " + nullSafe(report.getProjectName()), NORMAL_FONT));
        document.add(new Paragraph("Client : " + nullSafe(report.getClientName()), NORMAL_FONT));
        document.add(new Paragraph("SLA : " + nullSafe(report.getSlaName()), NORMAL_FONT));
        document.add(new Paragraph(
                "Période : " + format(report.getPeriodStart()) + " → " + format(report.getPeriodEnd()),
                NORMAL_FONT));
        document.add(new Paragraph("Généré le : " + format(report.getGeneratedAt()), NORMAL_FONT));
        if (report.getGeneratedByName() != null && !report.getGeneratedByName().isBlank()) {
            document.add(new Paragraph("Par : " + report.getGeneratedByName(), NORMAL_FONT));
        }
        document.add(new Paragraph(" ", NORMAL_FONT));
    }

    private void addKpiTable(Document document, ExecutiveReportKpiSummary kpi) throws DocumentException {
        if (kpi == null) {
            return;
        }
        document.add(new Paragraph("Indicateurs clés", SECTION_FONT));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.setSpacingAfter(12);

        addRow(table, "Score SLA", formatNumber(kpi.getSlaScore()) + " / 100");
        addRow(table, "Statut SLA", nullSafe(kpi.getSlaStatus()));
        addRow(table, "Disponibilité", formatNumber(kpi.getUptimePercentage()) + "% (cible "
                + formatNumber(kpi.getUptimeTarget()) + "%)");
        addRow(table, "Temps de réponse moyen", formatNumber(kpi.getAverageResponseTime()) + " ms");
        addRow(table, "Conformité temps de réponse", formatNumber(kpi.getResponseTimeCompliance()) + "%");
        addRow(table, "Taux d'erreur moyen", formatNumber(kpi.getAverageErrorRate()) + "%");
        addRow(table, "Incidents", String.valueOf(kpi.getIncidentCount()));
        addRow(table, "Incidents critiques", String.valueOf(kpi.getCriticalIncidentCount()));
        addRow(table, "Alertes", String.valueOf(kpi.getAlertCount()));
        addRow(table, "Services DOWN", String.valueOf(kpi.getServicesDown()));
        addRow(table, "Services dégradés", String.valueOf(kpi.getServicesDegraded()));

        document.add(table);
    }

    private void addCharts(Document document, PdfWriter writer, ExecutiveReportKpiSummary kpi)
            throws DocumentException {
        if (kpi == null) {
            return;
        }
        document.add(new Paragraph("Graphiques KPI", SECTION_FONT));
        document.add(new Paragraph(" ", SMALL_FONT));

        PdfPTable charts = new PdfPTable(2);
        charts.setWidthPercentage(100);
        charts.setSpacingAfter(16);

        charts.addCell(chartCell(writer, "Conformité vs cibles (%)", List.of(
                bar("Score", safe(kpi.getSlaScore()), new Color(79, 70, 229)),
                bar("Uptime", safe(kpi.getUptimePercentage()), new Color(16, 185, 129)),
                bar("Cible uptime", safe(kpi.getUptimeTarget()), new Color(148, 163, 184)),
                bar("RT conf.", safe(kpi.getResponseTimeCompliance()), new Color(59, 130, 246))
        ), 100));

        charts.addCell(chartCell(writer, "Incidents / alertes / services", List.of(
                bar("Incidents", safe(kpi.getIncidentCount()), new Color(249, 115, 22)),
                bar("Critiques", safe(kpi.getCriticalIncidentCount()), new Color(239, 68, 68)),
                bar("Alertes", safe(kpi.getAlertCount()), new Color(234, 179, 8)),
                bar("DOWN", safe(kpi.getServicesDown()), new Color(220, 38, 38)),
                bar("Dégradés", safe(kpi.getServicesDegraded()), new Color(168, 85, 247))
        ), null));

        document.add(charts);
    }

    private PdfPCell chartCell(PdfWriter writer, String title, List<BarItem> bars, Integer fixedMax)
            throws DocumentException {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setPadding(8);
        cell.addElement(new Paragraph(title, HEADER_FONT));

        float width = 230f;
        float height = 120f;
        PdfTemplate template = writer.getDirectContent().createTemplate(width, height);
        drawBarChart(template, bars, width, height, fixedMax);
        com.lowagie.text.Image image = com.lowagie.text.Image.getInstance(template);
        cell.addElement(image);
        return cell;
    }

    private void drawBarChart(PdfTemplate template, List<BarItem> bars, float width, float height,
                              Integer fixedMax) {
        PdfContentByte cb = template;
        float left = 28f;
        float bottom = 22f;
        float chartWidth = width - left - 8f;
        float chartHeight = height - bottom - 10f;

        double max = fixedMax != null ? fixedMax : bars.stream().mapToDouble(BarItem::value).max().orElse(1);
        if (max <= 0) {
            max = 1;
        }

        cb.setColorStroke(new Color(203, 213, 225));
        cb.moveTo(left, bottom);
        cb.lineTo(left + chartWidth, bottom);
        cb.stroke();

        float barWidth = chartWidth / Math.max(1, bars.size());
        float gap = barWidth * 0.25f;
        float usable = barWidth - gap;

        for (int i = 0; i < bars.size(); i++) {
            BarItem item = bars.get(i);
            float ratio = (float) (item.value() / max);
            float barHeight = Math.max(2f, chartHeight * Math.min(1f, ratio));
            float x = left + i * barWidth + gap / 2f;
            float y = bottom;

            cb.setColorFill(item.color());
            cb.rectangle(x, y, usable, barHeight);
            cb.fill();

            cb.beginText();
            cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA).getBaseFont(), 6.5f);
            cb.setColorFill(new Color(71, 85, 105));
            cb.showTextAligned(Element.ALIGN_CENTER, item.label(), x + usable / 2f, 8f, 0);
            cb.endText();
        }
    }

    private BarItem bar(String label, double value, Color color) {
        return new BarItem(label, value, color);
    }

    private double safe(Number value) {
        return value == null ? 0d : value.doubleValue();
    }

    private void addSection(Document document, String title, String body) throws DocumentException {
        document.add(new Paragraph(title, SECTION_FONT));
        Paragraph paragraph = new Paragraph(nullSafe(body), NORMAL_FONT);
        paragraph.setSpacingBefore(6);
        paragraph.setSpacingAfter(12);
        document.add(paragraph);
    }

    private void addRecommendations(Document document, List<String> recommendations) throws DocumentException {
        document.add(new Paragraph("5. AI Recommendations", SECTION_FONT));
        if (recommendations == null || recommendations.isEmpty()) {
            document.add(new Paragraph("Aucune recommandation.", NORMAL_FONT));
            return;
        }
        int index = 1;
        for (String item : recommendations) {
            Paragraph paragraph = new Paragraph(index + ". " + item, NORMAL_FONT);
            paragraph.setSpacingBefore(4);
            document.add(paragraph);
            index++;
        }
        document.add(new Paragraph(" ", NORMAL_FONT));
    }

    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setBackgroundColor(new Color(240, 240, 240));
        labelCell.setPadding(6);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setPadding(6);
        table.addCell(valueCell);
    }

    private String format(java.time.LocalDateTime value) {
        return value == null ? "—" : DATE_TIME.format(value);
    }

    private String formatNumber(Double value) {
        return value == null ? "—" : String.format("%.2f", value);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private record BarItem(String label, double value, Color color) {
    }
}
