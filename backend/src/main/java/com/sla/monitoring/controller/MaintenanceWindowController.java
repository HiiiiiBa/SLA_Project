package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.MaintenanceWindowCreateRequest;
import com.sla.monitoring.dto.request.MaintenanceWindowUpdateRequest;
import com.sla.monitoring.dto.response.MaintenanceWindowResponse;
import com.sla.monitoring.entity.enums.MaintenanceWindowStatus;
import com.sla.monitoring.service.MaintenanceWindowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/maintenance-windows")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Maintenance Windows", description = "Planned maintenance windows excluded from SLA calculations")
public class MaintenanceWindowController {

    private final MaintenanceWindowService maintenanceWindowService;

    @GetMapping
    @Operation(summary = "List maintenance windows with optional filters")
    public ResponseEntity<ApiResponse<List<MaintenanceWindowResponse>>> findAll(
            @RequestParam(required = false) Long slaId,
            @RequestParam(required = false) MaintenanceWindowStatus status) {
        if (slaId != null || status != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    maintenanceWindowService.findFiltered(slaId, status)));
        }
        return ResponseEntity.ok(ApiResponse.success(maintenanceWindowService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get maintenance window by ID")
    public ResponseEntity<ApiResponse<MaintenanceWindowResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(maintenanceWindowService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a maintenance window")
    public ResponseEntity<ApiResponse<MaintenanceWindowResponse>> create(
            @Valid @RequestBody MaintenanceWindowCreateRequest request) {
        MaintenanceWindowResponse response = maintenanceWindowService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Maintenance window created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a maintenance window")
    public ResponseEntity<ApiResponse<MaintenanceWindowResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceWindowUpdateRequest request) {
        MaintenanceWindowResponse response = maintenanceWindowService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Maintenance window updated successfully", response));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a maintenance window")
    public ResponseEntity<ApiResponse<MaintenanceWindowResponse>> cancel(@PathVariable Long id) {
        MaintenanceWindowResponse response = maintenanceWindowService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success("Maintenance window cancelled", response));
    }
}
