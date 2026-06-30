package com.sla.monitoring.exception;

/**
 * Thrown when authentication fails due to invalid credentials or disabled account.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}
