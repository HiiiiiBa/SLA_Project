package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.ProjectCreateRequest;
import com.sla.monitoring.dto.request.ProjectUpdateRequest;
import com.sla.monitoring.dto.response.ProjectResponse;
import com.sla.monitoring.service.ProjectService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Projects", description = "Client projects and team assignments")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "List projects")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> findAll(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long teamId) {
        if (clientId != null) {
            return ResponseEntity.ok(ApiResponse.success(projectService.findByClientId(clientId)));
        }
        if (teamId != null) {
            return ResponseEntity.ok(ApiResponse.success(projectService.findByTeamId(teamId)));
        }
        return ResponseEntity.ok(ApiResponse.success(projectService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a project")
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse response = projectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a project")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", projectService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully", null));
    }
}
