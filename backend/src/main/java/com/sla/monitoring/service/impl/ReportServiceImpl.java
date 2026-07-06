package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.ReportCreateRequest;
import com.sla.monitoring.dto.response.ReportResponse;
import com.sla.monitoring.entity.Report;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.exception.ForbiddenException;
import com.sla.monitoring.mapper.ReportMapper;
import com.sla.monitoring.report.CsvReportGenerator;
import com.sla.monitoring.report.PdfReportGenerator;
import com.sla.monitoring.report.ReportDataLoader;
import com.sla.monitoring.report.model.ReportExportData;
import com.sla.monitoring.report.model.ReportExportResult;
import com.sla.monitoring.repository.ReportRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.service.ClientScopeService;
import com.sla.monitoring.service.EmployeeScopeService;
import com.sla.monitoring.service.ManagerScopeService;
import com.sla.monitoring.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final SlaRepository slaRepository;
    private final ReportMapper reportMapper;
    private final ReportDataLoader reportDataLoader;
    private final PdfReportGenerator pdfReportGenerator;
    private final CsvReportGenerator csvReportGenerator;
    private final EmployeeScopeService employeeScopeService;
    private final ManagerScopeService managerScopeService;
    private final ClientScopeService clientScopeService;

    @Override
    @Transactional
    public ReportResponse createReport(ReportCreateRequest request) {
        if (request.getPeriodStart().isAfter(request.getPeriodEnd())) {
            throw new BusinessException("Period start must be before period end");
        }

        Sla sla = findSlaById(request.getSlaId());
        managerScopeService.assertSlaAccess(sla.getId());

        Report report = reportMapper.toEntity(request);
        report.setSla(sla);
        report.setGeneratedAt(LocalDateTime.now());

        return reportMapper.toResponse(reportRepository.save(report));
    }

    @Override
    public List<ReportResponse> findAll() {
        if (employeeScopeService.isCurrentUserEmployee()) {
            return List.of();
        }
        if (clientScopeService.isCurrentUserClient()) {
            Set<Long> slaIds = clientScopeService.getScopedSlaIds();
            if (slaIds.isEmpty()) {
                return List.of();
            }
            return reportRepository.findBySlaIdIn(slaIds).stream()
                    .map(reportMapper::toResponse)
                    .toList();
        }
        if (managerScopeService.isCurrentUserManager()) {
            Set<Long> slaIds = managerScopeService.getScopedSlaIds();
            if (slaIds.isEmpty()) {
                return List.of();
            }
            return reportRepository.findBySlaIdIn(slaIds).stream()
                    .map(reportMapper::toResponse)
                    .toList();
        }
        return reportRepository.findAll().stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Override
    public ReportResponse findById(Long id) {
        if (employeeScopeService.isCurrentUserEmployee()) {
            throw new ForbiddenException("Employees cannot access reports");
        }
        Report report = findReportEntityById(id);
        managerScopeService.assertSlaAccess(report.getSla().getId());
        clientScopeService.assertSlaAccess(report.getSla().getId());
        return reportMapper.toResponse(report);
    }

    @Override
    @Transactional
    public void deleteReport(Long id) {
        Report report = findReportEntityById(id);
        employeeScopeService.assertSlaAccess(report.getSla().getId());
        managerScopeService.assertSlaAccess(report.getSla().getId());
        clientScopeService.assertSlaAccess(report.getSla().getId());
        reportRepository.delete(report);
    }

    @Override
    public ReportExportResult exportPdf(Long reportId) {
        assertReportAccess(reportId);
        ReportExportData data = reportDataLoader.load(reportId);
        return pdfReportGenerator.generate(data);
    }

    @Override
    public ReportExportResult exportCsv(Long reportId) {
        assertReportAccess(reportId);
        ReportExportData data = reportDataLoader.load(reportId);
        return csvReportGenerator.generate(data);
    }

    private void assertReportAccess(Long reportId) {
        if (employeeScopeService.isCurrentUserEmployee()) {
            throw new ForbiddenException("Employees cannot access reports");
        }
        Report report = findReportEntityById(reportId);
        managerScopeService.assertSlaAccess(report.getSla().getId());
        clientScopeService.assertSlaAccess(report.getSla().getId());
    }

    private Report findReportEntityById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
    }

    private Sla findSlaById(Long id) {
        return slaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", id));
    }
}
