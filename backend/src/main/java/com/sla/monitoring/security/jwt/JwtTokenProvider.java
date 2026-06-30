package com.sla.monitoring.security.jwt;

import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.exception.ExpiredTokenException;
import com.sla.monitoring.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * Low-level JWT creation and parsing with custom claims.
 */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    /**
     * Generates a signed access token for the given user.
     */
    public String generateAccessToken(Long userId, String email, Role role) {
        return buildToken(userId, email, role, accessTokenExpiration, TOKEN_TYPE_ACCESS);
    }

    /**
     * Generates a signed refresh token for the given user.
     */
    public String generateRefreshTokenValue(Long userId, String email, Role role) {
        return buildToken(userId, email, role, refreshTokenExpiration, TOKEN_TYPE_REFRESH);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_USER_ID, Long.class));
    }

    public Role extractRole(String token) {
        String role = extractClaim(token, claims -> claims.get(CLAIM_ROLE, String.class));
        return Role.valueOf(role);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(extractTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(extractTokenType(token));
    }

    public void validateAccessToken(String token) {
        validateToken(token, TOKEN_TYPE_ACCESS);
    }

    public void validateRefreshToken(String token) {
        validateToken(token, TOKEN_TYPE_REFRESH);
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpiration;
    }

    private void validateToken(String token, String expectedType) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            if (!expectedType.equals(tokenType)) {
                throw new InvalidTokenException("Invalid token type");
            }
        } catch (ExpiredJwtException ex) {
            throw new ExpiredTokenException("JWT token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid JWT token");
        }
    }

    private String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    private String buildToken(Long userId, String email, Role role, long expirationMs, String tokenType) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSignInKey())
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new ExpiredTokenException("JWT token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid JWT token");
        }
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
