package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.ClientCreateRequest;
import com.sla.monitoring.dto.request.ClientUpdateRequest;
import com.sla.monitoring.dto.response.ClientResponse;
import com.sla.monitoring.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for client management.
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Clients", description = "Client CRUD operations")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    @Operation(summary = "Get all clients")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(clientService.getAllClients()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<ApiResponse<ClientResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(clientService.getClient(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new client")
    public ResponseEntity<ApiResponse<ClientResponse>> create(
            @Valid @RequestBody ClientCreateRequest request) {
        ClientResponse response = clientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Client created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing client")
    public ResponseEntity<ApiResponse<ClientResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientUpdateRequest request) {
        ClientResponse response = clientService.updateClient(id, request);
        return ResponseEntity.ok(ApiResponse.success("Client updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a client")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok(ApiResponse.success("Client deleted successfully", null));
    }
}
