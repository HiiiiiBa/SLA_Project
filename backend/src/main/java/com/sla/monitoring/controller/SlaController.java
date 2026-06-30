package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.SlaCreateRequest;
import com.sla.monitoring.dto.request.SlaUpdateRequest;
import com.sla.monitoring.dto.response.SlaResponse;
import com.sla.monitoring.service.SlaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * REST endpoints for SLA management.
 */
@RestController
@RequestMapping("/api/slas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "SLAs", description = "SLA CRUD and lifecycle operations")
public class SlaController {

    private final SlaService slaService;

    @GetMapping
    @Operation(summary = "Get all SLAs")
    public ResponseEntity<ApiResponse<List<SlaResponse>>> getAll(
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(slaService.getAll(clientId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get SLA by ID")
    public ResponseEntity<ApiResponse<SlaResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(slaService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new SLA")
    public ResponseEntity<ApiResponse<SlaResponse>> create(
            @Valid @RequestBody SlaCreateRequest request) {
        SlaResponse response = slaService.createSLA(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SLA created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing SLA")
    public ResponseEntity<ApiResponse<SlaResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SlaUpdateRequest request) {
        SlaResponse response = slaService.updateSLA(id, request);
        return ResponseEntity.ok(ApiResponse.success("SLA updated successfully", response));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive an SLA")
    public ResponseEntity<ApiResponse<SlaResponse>> archive(@PathVariable Long id) {
        SlaResponse response = slaService.archiveSLA(id);
        return ResponseEntity.ok(ApiResponse.success("SLA archived successfully", response));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate an SLA")
    public ResponseEntity<ApiResponse<SlaResponse>> activate(@PathVariable Long id) {
        SlaResponse response = slaService.activateSLA(id);
        return ResponseEntity.ok(ApiResponse.success("SLA activated successfully", response));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate an SLA")
    public ResponseEntity<ApiResponse<SlaResponse>> deactivate(@PathVariable Long id) {
        SlaResponse response = slaService.deactivateSLA(id);
        return ResponseEntity.ok(ApiResponse.success("SLA deactivated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an SLA")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        slaService.deleteSLA(id);
        return ResponseEntity.ok(ApiResponse.success("SLA deleted successfully", null));
    }
}
