package com.sla.monitoring.exception;

/**
 * Thrown when an authenticated user lacks permission for a resource.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
