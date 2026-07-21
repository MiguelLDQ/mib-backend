package com.mib.backend.exception;

public class AnonymousMessageNotFoundException extends RuntimeException {
    public AnonymousMessageNotFoundException(String message) {
        super(message);
    }
}
