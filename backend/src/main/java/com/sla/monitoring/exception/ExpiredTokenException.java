package com.sla.monitoring.exception;

public class ExpiredTokenException extends UnauthorizedException {

    public ExpiredTokenException(String message) {
        super(message);
    }
}
