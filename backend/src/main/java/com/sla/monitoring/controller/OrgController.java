package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.response.UserResponse;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Organization", description = "Managers and employees lookup")
public class OrgController {

    private final UserService userService;

    @GetMapping("/users")
    @Operation(summary = "List organization users by role")
    public ResponseEntity<ApiResponse<List<UserResponse>>> findOrgUsers(
            @RequestParam(required = false) Role role) {
        if (role != null) {
            return ResponseEntity.ok(ApiResponse.success(userService.findByRole(role)));
        }
        java.util.List<UserResponse> users = new java.util.ArrayList<>();
        users.addAll(userService.findByRole(Role.MANAGER));
        users.addAll(userService.findByRole(Role.EMPLOYEE));
        return ResponseEntity.ok(ApiResponse.success(users));
    }
}
