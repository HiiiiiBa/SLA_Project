package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.ReportCreateRequest;
import com.sla.monitoring.dto.response.ReportResponse;
import com.sla.monitoring.report.model.ReportExportResult;

import java.util.List;

public interface ReportService {

    ReportResponse createReport(ReportCreateRequest request);

    List<ReportResponse> findAll();

    ReportResponse findById(Long id);

    void deleteReport(Long id);

    ReportExportResult exportPdf(Long reportId);

    ReportExportResult exportCsv(Long reportId);
}
