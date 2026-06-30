package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.response.SlaEvaluationResponse;
import com.sla.monitoring.service.SlaEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin endpoints to manually trigger SLA engine evaluation.
 */
@RestController
@RequestMapping("/api/admin/sla-engine")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "SLA Engine", description = "Manual SLA calculation triggers (ADMIN only)")
public class SlaEngineController {

    private final SlaEngineService slaEngineService;

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate all active SLAs")
    public ResponseEntity<ApiResponse<List<SlaEvaluationResponse>>> evaluateAll() {
        List<SlaEvaluationResponse> results = slaEngineService.evaluateAll();
        return ResponseEntity.ok(ApiResponse.success("SLA evaluation completed", results));
    }

    @PostMapping("/evaluate/{slaId}")
    @Operation(summary = "Evaluate a single SLA by ID")
    public ResponseEntity<ApiResponse<SlaEvaluationResponse>> evaluateById(@PathVariable Long slaId) {
        SlaEvaluationResponse result = slaEngineService.evaluateById(slaId);
        return ResponseEntity.ok(ApiResponse.success("SLA evaluation completed", result));
    }
}
