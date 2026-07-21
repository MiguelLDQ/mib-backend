package com.mib.backend.exception;

public class CannotLikeOwnMessageException extends RuntimeException {
    public CannotLikeOwnMessageException(String message) {
        super(message);
    }
}
