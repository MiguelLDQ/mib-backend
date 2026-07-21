package com.mib.backend.exception;

public class MessageBlockedByModerationException extends RuntimeException {
    public MessageBlockedByModerationException(String message) {
        super(message);
    }
}
