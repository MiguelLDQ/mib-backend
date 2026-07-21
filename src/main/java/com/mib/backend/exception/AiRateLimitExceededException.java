package com.mib.backend.exception;

public class AiRateLimitExceededException extends RuntimeException {
    public AiRateLimitExceededException(String message) {
        super(message);
    }
}
