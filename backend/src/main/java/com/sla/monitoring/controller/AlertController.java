package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.AlertCreateRequest;
import com.sla.monitoring.dto.response.AlertResponse;
import com.sla.monitoring.entity.enums.AlertStatus;
import com.sla.monitoring.entity.enums.AlertType;
import com.sla.monitoring.service.AlertService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for alert management.
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Alerts", description = "Alert CRUD and status operations")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @Operation(summary = "Get alerts with optional filters")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> findAll(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long slaId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) AlertType type,
            @RequestParam(required = false) AlertStatus status) {
        if (Boolean.TRUE.equals(active)) {
            return ResponseEntity.ok(ApiResponse.success(alertService.findActiveAlerts()));
        }
        if (slaId != null || serviceId != null || type != null || status != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    alertService.findFiltered(slaId, serviceId, type, status)));
        }
        return ResponseEntity.ok(ApiResponse.success(alertService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get alert by ID")
    public ResponseEntity<ApiResponse<AlertResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(alertService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new alert")
    public ResponseEntity<ApiResponse<AlertResponse>> create(
            @Valid @RequestBody AlertCreateRequest request) {
        AlertResponse response = alertService.createAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Alert created successfully", response));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark alert as read")
    public ResponseEntity<ApiResponse<AlertResponse>> markAsRead(@PathVariable Long id) {
        AlertResponse response = alertService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Alert marked as read", response));
    }

    @PatchMapping("/{id}/resolve")
    @Operation(summary = "Resolve an alert")
    public ResponseEntity<ApiResponse<AlertResponse>> resolve(@PathVariable Long id) {
        AlertResponse response = alertService.resolveAlert(id);
        return ResponseEntity.ok(ApiResponse.success("Alert resolved successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an alert")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return ResponseEntity.ok(ApiResponse.success("Alert deleted successfully", null));
    }
}
