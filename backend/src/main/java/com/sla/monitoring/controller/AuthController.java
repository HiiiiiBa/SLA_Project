package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.ChangePasswordRequest;
import com.sla.monitoring.dto.request.LoginRequest;
import com.sla.monitoring.dto.request.RefreshTokenRequest;
import com.sla.monitoring.dto.request.RegisterRequest;
import com.sla.monitoring.dto.response.AuthenticationResponse;
import com.sla.monitoring.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for authentication and account management.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, token refresh and password management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthenticationResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Authenticate user and obtain JWT tokens")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Refresh access token using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthenticationResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Revoke refresh token and logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @PutMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change password for authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}
