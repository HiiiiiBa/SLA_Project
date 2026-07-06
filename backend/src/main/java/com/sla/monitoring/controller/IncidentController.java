package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.IncidentCreateRequest;
import com.sla.monitoring.dto.request.IncidentUpdateRequest;
import com.sla.monitoring.dto.response.IncidentResponse;
import com.sla.monitoring.entity.enums.IncidentSeverity;
import com.sla.monitoring.service.IncidentService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for incident management.
 */
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Incidents", description = "Incident CRUD and lifecycle operations")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @Operation(summary = "Get incidents (all, open, or filtered by severity)")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> findAll(
            @RequestParam(required = false) Long slaId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Boolean open,
            @RequestParam(required = false) IncidentSeverity severity) {
        if (slaId != null) {
            return ResponseEntity.ok(ApiResponse.success(incidentService.findBySlaId(slaId)));
        }
        if (projectId != null) {
            return ResponseEntity.ok(ApiResponse.success(incidentService.findByProjectId(projectId)));
        }
        if (Boolean.TRUE.equals(open)) {
            return ResponseEntity.ok(ApiResponse.success(incidentService.findOpenIncidents()));
        }
        if (severity != null) {
            return ResponseEntity.ok(ApiResponse.success(incidentService.findBySeverity(severity)));
        }
        return ResponseEntity.ok(ApiResponse.success(incidentService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident by ID")
    public ResponseEntity<ApiResponse<IncidentResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(incidentService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new incident")
    public ResponseEntity<ApiResponse<IncidentResponse>> create(
            @Valid @RequestBody IncidentCreateRequest request) {
        IncidentResponse response = incidentService.createIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Incident created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing incident")
    public ResponseEntity<ApiResponse<IncidentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody IncidentUpdateRequest request) {
        IncidentResponse response = incidentService.updateIncident(id, request);
        return ResponseEntity.ok(ApiResponse.success("Incident updated successfully", response));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Close an incident")
    public ResponseEntity<ApiResponse<IncidentResponse>> close(@PathVariable Long id) {
        IncidentResponse response = incidentService.closeIncident(id);
        return ResponseEntity.ok(ApiResponse.success("Incident closed successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an incident")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.ok(ApiResponse.success("Incident deleted successfully", null));
    }
}
