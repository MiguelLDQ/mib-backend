package com.mib.backend.exception;

public class DuplicateReportException extends RuntimeException {
    public DuplicateReportException(String message) {
        super(message);
    }
}
