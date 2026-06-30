package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.MonitoringMetricCreateRequest;
import com.sla.monitoring.dto.response.MonitoringMetricResponse;
import com.sla.monitoring.service.MonitoringMetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST endpoints for monitoring metrics.
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Monitoring Metrics", description = "Monitoring metric operations")
public class MonitoringMetricController {

    private final MonitoringMetricService monitoringMetricService;

    @GetMapping
    @Operation(summary = "Get monitoring metrics (all, by service, or by date range)")
    public ResponseEntity<ApiResponse<List<MonitoringMetricResponse>>> findAll(
            @RequestParam(required = false) Long slaId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        if (slaId != null) {
            return ResponseEntity.ok(ApiResponse.success(monitoringMetricService.findBySla(slaId)));
        }
        if (serviceId != null) {
            return ResponseEntity.ok(ApiResponse.success(monitoringMetricService.findByService(serviceId)));
        }
        if (start != null && end != null) {
            return ResponseEntity.ok(ApiResponse.success(monitoringMetricService.findByDateRange(start, end)));
        }
        return ResponseEntity.ok(ApiResponse.success(monitoringMetricService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get monitoring metric by ID")
    public ResponseEntity<ApiResponse<MonitoringMetricResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(monitoringMetricService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Add a monitoring metric")
    public ResponseEntity<ApiResponse<MonitoringMetricResponse>> add(
            @Valid @RequestBody MonitoringMetricCreateRequest request) {
        MonitoringMetricResponse response = monitoringMetricService.addMetric(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Metric added successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a monitoring metric")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        monitoringMetricService.deleteMetric(id);
        return ResponseEntity.ok(ApiResponse.success("Metric deleted successfully", null));
    }
}
