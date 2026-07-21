package com.mib.backend.exception;

public class MissionAlreadyCompletedException extends RuntimeException {
    public MissionAlreadyCompletedException(String message) {
        super(message);
    }
}
