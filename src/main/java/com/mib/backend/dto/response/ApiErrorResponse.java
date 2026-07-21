package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> fieldErrors
) {
    public record FieldErrorDetail(String field, String message) {
    }

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, List.of());
    }

    public static ApiErrorResponse of(int status, String error, String message, String path,
                                       List<FieldErrorDetail> fieldErrors) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, fieldErrors);
    }
}
