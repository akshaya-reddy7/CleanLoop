package com.cleanloop.exception;

import org.springframework.http.HttpStatus;

/**
 * Base application exception carrying HTTP status.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
