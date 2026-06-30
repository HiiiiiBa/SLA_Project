package com.sla.monitoring.report;

import com.sla.monitoring.engine.SlaCalculator;
import com.sla.monitoring.engine.model.SlaEvaluationResult;
import com.sla.monitoring.entity.Client;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Report;
import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.IncidentSeverity;
import com.sla.monitoring.entity.enums.MetricStatus;
import com.sla.monitoring.entity.enums.ReportFormat;
import com.sla.monitoring.entity.enums.ServiceStatus;
import com.sla.monitoring.entity.enums.SlaStatus;
import com.sla.monitoring.report.model.ReportExportData;
import com.sla.monitoring.report.model.ReportExportResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvReportGeneratorTest {

    private CsvReportGenerator csvReportGenerator;
    private ReportExportData exportData;

    @BeforeEach
    void setUp() {
        csvReportGenerator = new CsvReportGenerator();

        LocalDateTime periodEnd = LocalDateTime.of(2026, 6, 30, 12, 0);
        LocalDateTime periodStart = periodEnd.minusHours(24);

        Client client = Client.builder()
                .id(1L)
                .name("Acme Corp")
                .email("client@acme.com")
                .projectName("Production Platform")
                .build();

        Sla sla = Sla.builder()
                .id(2L)
                .name("Production API")
                .status(SlaStatus.ACTIVE)
                .uptimeTarget(99.5)
                .responseTimeLimit(500)
                .errorRateLimit(1.0)
                .client(client)
                .build();

        Service service = Service.builder()
                .id(3L)
                .name("API Gateway")
                .status(ServiceStatus.UP)
                .sla(sla)
                .build();

        List<MonitoringMetric> metrics = List.of(
                MonitoringMetric.builder()
                        .timestamp(periodStart.plusHours(1))
                        .responseTime(120.0)
                        .errorRate(0.2)
                        .status(MetricStatus.UP)
                        .service(service)
                        .sla(sla)
                        .build()
        );

        List<Incident> incidents = List.of(
                Incident.builder()
                        .startTime(periodStart.plusHours(2))
                        .endTime(periodStart.plusHours(3))
                        .severity(IncidentSeverity.MEDIUM)
                        .description("Database latency spike")
                        .sla(sla)
                        .build()
        );

        SlaEvaluationResult evaluation = new SlaCalculator().evaluate(
                sla, metrics, incidents, periodStart, periodEnd);

        Report report = Report.builder()
                .id(10L)
                .slaResult(evaluation.getSlaScore())
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .generatedAt(LocalDateTime.now())
                .format(ReportFormat.CSV)
                .sla(sla)
                .build();

        exportData = ReportExportData.builder()
                .report(report)
                .sla(sla)
                .client(client)
                .evaluation(evaluation)
                .metrics(metrics)
                .incidents(incidents)
                .alerts(List.of())
                .build();
    }

    @Test
    @DisplayName("Generates CSV with summary, metrics and incidents")
    void generateReturnsCsvContent() {
        ReportExportResult result = csvReportGenerator.generate(exportData);
        String csv = new String(result.content(), StandardCharsets.UTF_8);

        assertThat(result.contentType()).isEqualTo("text/csv");
        assertThat(result.filename()).isEqualTo("sla-report-10.csv");
        assertThat(csv).contains("section,key,value");
        assertThat(csv).contains("\"summary\",\"client_name\",\"Acme Corp\"");
        assertThat(csv).contains("timestamp,service,status,response_time_ms,error_rate_percent");
        assertThat(csv).contains("\"API Gateway\"");
        assertThat(csv).contains("start_time,end_time,severity,description");
        assertThat(csv).contains("Database latency spike");
    }
}
