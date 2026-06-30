package com.sla.monitoring.security.util;

import com.sla.monitoring.exception.AuthenticationException;
import com.sla.monitoring.security.service.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility methods for accessing the current authenticated user.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new AuthenticationException("User is not authenticated");
        }
        return details;
    }

    public static String getCurrentUserEmail() {
        return getCurrentUserDetails().getUsername();
    }

    public static Long getCurrentUserId() {
        return getCurrentUserDetails().getUser().getId();
    }
}
