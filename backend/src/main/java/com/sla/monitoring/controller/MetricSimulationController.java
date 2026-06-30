package com.sla.monitoring.controller;

import com.sla.monitoring.config.MetricSimulationProperties;
import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.response.MetricSimulationResponse;
import com.sla.monitoring.service.MetricSimulationService;
import com.sla.monitoring.simulation.SimulationScenario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoints to manually trigger monitoring metric simulation.
 */
@RestController
@RequestMapping("/api/admin/metrics/simulate")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Metric Simulation", description = "Generate simulated UP/DOWN metrics (ADMIN only)")
public class MetricSimulationController {

    private final MetricSimulationService metricSimulationService;
    private final MetricSimulationProperties metricSimulationProperties;

    @PostMapping
    @Operation(summary = "Simulate metrics for all active services")
    public ResponseEntity<ApiResponse<MetricSimulationResponse>> simulateAll(
            @RequestParam(required = false) SimulationScenario scenario) {
        SimulationScenario selectedScenario = resolveScenario(scenario);
        MetricSimulationResponse response = metricSimulationService.simulateAll(selectedScenario);
        return ResponseEntity.ok(ApiResponse.success("Metric simulation completed", response));
    }

    @PostMapping("/sla/{slaId}")
    @Operation(summary = "Simulate metrics for all services of an SLA")
    public ResponseEntity<ApiResponse<MetricSimulationResponse>> simulateForSla(
            @PathVariable Long slaId,
            @RequestParam(required = false) SimulationScenario scenario) {
        SimulationScenario selectedScenario = resolveScenario(scenario);
        MetricSimulationResponse response = metricSimulationService.simulateForSla(slaId, selectedScenario);
        return ResponseEntity.ok(ApiResponse.success("Metric simulation completed", response));
    }

    @PostMapping("/service/{serviceId}")
    @Operation(summary = "Simulate a metric for a single service")
    public ResponseEntity<ApiResponse<MetricSimulationResponse>> simulateForService(
            @PathVariable Long serviceId,
            @RequestParam(required = false) SimulationScenario scenario) {
        SimulationScenario selectedScenario = resolveScenario(scenario);
        MetricSimulationResponse response = metricSimulationService.simulateForService(serviceId, selectedScenario);
        return ResponseEntity.ok(ApiResponse.success("Metric simulation completed", response));
    }

    private SimulationScenario resolveScenario(SimulationScenario scenario) {
        return scenario != null ? scenario : metricSimulationProperties.getScenario();
    }
}
