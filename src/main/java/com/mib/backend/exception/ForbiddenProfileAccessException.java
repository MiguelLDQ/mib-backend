package com.mib.backend.exception;

public class ForbiddenProfileAccessException extends RuntimeException {
    public ForbiddenProfileAccessException(String message) {
        super(message);
    }
}
