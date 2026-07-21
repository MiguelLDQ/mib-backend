package com.mib.backend.exception;

public class CannotFriendSelfException extends RuntimeException {
    public CannotFriendSelfException(String message) {
        super(message);
    }
}
