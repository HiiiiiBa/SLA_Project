package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.ReportCreateRequest;
import com.sla.monitoring.dto.response.ReportResponse;
import com.sla.monitoring.report.model.ReportExportResult;
import com.sla.monitoring.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * REST endpoints for SLA report management.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reports", description = "SLA report CRUD and export operations")
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    @Operation(summary = "Get all reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(reportService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get report by ID")
    public ResponseEntity<ApiResponse<ReportResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reportService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new report")
    public ResponseEntity<ApiResponse<ReportResponse>> create(
            @Valid @RequestBody ReportCreateRequest request) {
        ReportResponse response = reportService.createReport(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report created successfully", response));
    }

    @GetMapping("/{id}/export/pdf")
    @Operation(summary = "Export report as PDF")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        ReportExportResult export = reportService.exportPdf(id);
        return fileResponse(export);
    }

    @GetMapping("/{id}/export/csv")
    @Operation(summary = "Export report as CSV")
    public ResponseEntity<byte[]> exportCsv(@PathVariable Long id) {
        ReportExportResult export = reportService.exportCsv(id);
        return fileResponse(export);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a report")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report deleted successfully", null));
    }

    private ResponseEntity<byte[]> fileResponse(ReportExportResult export) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(export.contentType()));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(export.filename(), StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(export.content().length);
        return new ResponseEntity<>(export.content(), headers, HttpStatus.OK);
    }
}
