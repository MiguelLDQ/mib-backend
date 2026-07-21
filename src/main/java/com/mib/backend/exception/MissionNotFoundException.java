package com.mib.backend.exception;

public class MissionNotFoundException extends RuntimeException {
    public MissionNotFoundException(String message) {
        super(message);
    }
}
