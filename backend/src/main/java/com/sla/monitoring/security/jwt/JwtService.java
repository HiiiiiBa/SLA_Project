package com.sla.monitoring.security.jwt;

import com.sla.monitoring.entity.User;
import com.sla.monitoring.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Facade service for JWT operations used by authentication flow and filters.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtTokenProvider jwtTokenProvider;

    public String generateAccessToken(UserDetails userDetails) {
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();
        return jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
    }

    public String generateAccessToken(User user) {
        return jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
    }

    public String generateRefreshTokenValue(User user) {
        return jwtTokenProvider.generateRefreshTokenValue(user.getId(), user.getEmail(), user.getRole());
    }

    public String extractEmail(String token) {
        return jwtTokenProvider.extractEmail(token);
    }

    public Long extractUserId(String token) {
        return jwtTokenProvider.extractUserId(token);
    }

    public Date extractExpiration(String token) {
        return jwtTokenProvider.extractExpiration(token);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        jwtTokenProvider.validateAccessToken(token);
        return jwtTokenProvider.extractEmail(token).equals(userDetails.getUsername());
    }

    public void validateAccessToken(String token) {
        jwtTokenProvider.validateAccessToken(token);
    }

    public void validateRefreshToken(String token) {
        jwtTokenProvider.validateRefreshToken(token);
    }

    public long getAccessTokenExpirationMs() {
        return jwtTokenProvider.getAccessTokenExpirationMs();
    }

    public long getRefreshTokenExpirationMs() {
        return jwtTokenProvider.getRefreshTokenExpirationMs();
    }
}
