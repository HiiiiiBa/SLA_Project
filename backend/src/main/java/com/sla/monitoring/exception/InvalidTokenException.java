package com.sla.monitoring.exception;

public class InvalidTokenException extends UnauthorizedException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
