package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.ChangePasswordRequest;
import com.sla.monitoring.dto.request.LoginRequest;
import com.sla.monitoring.dto.request.RefreshTokenRequest;
import com.sla.monitoring.dto.request.RegisterRequest;
import com.sla.monitoring.dto.response.AuthenticationResponse;

/**
 * Authentication and authorization service contract.
 */
public interface AuthService {

    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse login(LoginRequest request);

    AuthenticationResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void changePassword(ChangePasswordRequest request);
}
