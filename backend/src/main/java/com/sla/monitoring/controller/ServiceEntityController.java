package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.ServiceEntityCreateRequest;
import com.sla.monitoring.dto.request.ServiceEntityUpdateRequest;
import com.sla.monitoring.dto.request.ServiceStatusChangeRequest;
import com.sla.monitoring.dto.response.ServiceEntityResponse;
import com.sla.monitoring.service.ServiceEntityService;
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

import java.util.List;

/**
 * REST endpoints for monitored service management.
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Services", description = "Monitored service CRUD operations")
public class ServiceEntityController {

    private final ServiceEntityService serviceEntityService;

    @GetMapping
    @Operation(summary = "Get all services")
    public ResponseEntity<ApiResponse<List<ServiceEntityResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(serviceEntityService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service by ID")
    public ResponseEntity<ApiResponse<ServiceEntityResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(serviceEntityService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new service")
    public ResponseEntity<ApiResponse<ServiceEntityResponse>> create(
            @Valid @RequestBody ServiceEntityCreateRequest request) {
        ServiceEntityResponse response = serviceEntityService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing service")
    public ResponseEntity<ApiResponse<ServiceEntityResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ServiceEntityUpdateRequest request) {
        ServiceEntityResponse response = serviceEntityService.updateService(id, request);
        return ResponseEntity.ok(ApiResponse.success("Service updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change service status")
    public ResponseEntity<ApiResponse<ServiceEntityResponse>> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ServiceStatusChangeRequest request) {
        ServiceEntityResponse response = serviceEntityService.changeStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Service status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a service")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        serviceEntityService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success("Service deleted successfully", null));
    }
}
