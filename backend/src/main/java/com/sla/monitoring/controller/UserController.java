package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.UserCreateRequest;
import com.sla.monitoring.dto.request.UserUpdateRequest;
import com.sla.monitoring.dto.response.UserResponse;
import com.sla.monitoring.service.UserService;
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
 * REST endpoints for user management (admin only).
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "User management (ADMIN only)")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(userService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.findById(id)));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    public ResponseEntity<ApiResponse<UserResponse>> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ApiResponse.success(userService.findByEmail(email)));
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
