package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.AiChatRequest;
import com.sla.monitoring.dto.request.ExecutiveReportRequest;
import com.sla.monitoring.dto.response.AiChatResponse;
import com.sla.monitoring.dto.response.ExecutiveReportListItemResponse;
import com.sla.monitoring.dto.response.ExecutiveReportResponse;
import com.sla.monitoring.dto.response.IncidentAnalysisResponse;
import com.sla.monitoring.report.model.ReportExportResult;
import com.sla.monitoring.service.AiChatService;
import com.sla.monitoring.service.ExecutiveReportAiService;
import com.sla.monitoring.service.IncidentAiService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "AI", description = "Gemini-powered incident analysis, chatbot and executive reports")
public class AiController {

    private final IncidentAiService incidentAiService;
    private final AiChatService aiChatService;
    private final ExecutiveReportAiService executiveReportAiService;

    @PostMapping("/incidents/{id}/analyze")
    @Operation(summary = "Analyze an incident with Gemini AI")
    public ResponseEntity<ApiResponse<IncidentAnalysisResponse>> analyzeIncident(@PathVariable Long id) {
        IncidentAnalysisResponse response = incidentAiService.analyzeIncident(id);
        return ResponseEntity.ok(ApiResponse.success("Analyse générée avec succès", response));
    }

    @PostMapping("/ai/chat")
    @Operation(summary = "Chat with the SLA Monitor AI assistant")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = aiChatService.chat(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ai/executive-report")
    @Operation(summary = "Generate and persist an AI executive report")
    public ResponseEntity<ApiResponse<ExecutiveReportResponse>> generateExecutiveReport(
            @Valid @RequestBody ExecutiveReportRequest request) {
        ExecutiveReportResponse response = executiveReportAiService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rapport exécutif généré avec succès", response));
    }

    @GetMapping("/ai/executive-report")
    @Operation(summary = "List saved AI executive reports")
    public ResponseEntity<ApiResponse<List<ExecutiveReportListItemResponse>>> listExecutiveReports(
            @RequestParam(required = false) Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(executiveReportAiService.findAll(projectId)));
    }

    @GetMapping("/ai/executive-report/{id}")
    @Operation(summary = "Get a saved AI executive report")
    public ResponseEntity<ApiResponse<ExecutiveReportResponse>> getExecutiveReport(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(executiveReportAiService.findById(id)));
    }

    @GetMapping("/ai/executive-report/{id}/export/pdf")
    @Operation(summary = "Export a saved AI executive report as PDF")
    public ResponseEntity<byte[]> exportExecutiveReportPdfById(@PathVariable Long id) {
        return fileResponse(executiveReportAiService.exportPdfById(id));
    }

    @PostMapping("/ai/executive-report/export/pdf")
    @Operation(summary = "Export an AI executive report payload as PDF")
    public ResponseEntity<byte[]> exportExecutiveReportPdf(
            @Valid @RequestBody ExecutiveReportResponse report) {
        return fileResponse(executiveReportAiService.exportPdf(report));
    }

    @DeleteMapping("/ai/executive-report/{id}")
    @Operation(summary = "Delete a saved AI executive report (admin)")
    public ResponseEntity<ApiResponse<Void>> deleteExecutiveReport(@PathVariable Long id) {
        executiveReportAiService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Rapport IA supprimé", null));
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
