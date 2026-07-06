package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.TeamCreateRequest;
import com.sla.monitoring.dto.request.TeamUpdateRequest;
import com.sla.monitoring.dto.response.TeamResponse;
import com.sla.monitoring.service.TeamService;
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
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Teams", description = "Team management (manager + employees)")
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    @Operation(summary = "List teams")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> findAll(
            @RequestParam(required = false) Long managerId) {
        if (managerId != null) {
            return ResponseEntity.ok(ApiResponse.success(teamService.findByManagerId(managerId)));
        }
        return ResponseEntity.ok(ApiResponse.success(teamService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID")
    public ResponseEntity<ApiResponse<TeamResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(teamService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a team")
    public ResponseEntity<ApiResponse<TeamResponse>> create(@Valid @RequestBody TeamCreateRequest request) {
        TeamResponse response = teamService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a team")
    public ResponseEntity<ApiResponse<TeamResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TeamUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Team updated successfully", teamService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a team")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", null));
    }
}
