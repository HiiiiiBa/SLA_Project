package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.ExecutiveReportRequest;
import com.sla.monitoring.dto.response.ExecutiveReportListItemResponse;
import com.sla.monitoring.dto.response.ExecutiveReportResponse;
import com.sla.monitoring.report.model.ReportExportResult;

import java.util.List;

public interface ExecutiveReportAiService {

    ExecutiveReportResponse generate(ExecutiveReportRequest request);

    List<ExecutiveReportListItemResponse> findAll(Long projectId);

    ExecutiveReportResponse findById(Long id);

    ReportExportResult exportPdfById(Long id);

    ReportExportResult exportPdf(ExecutiveReportResponse report);

    void delete(Long id);
}
