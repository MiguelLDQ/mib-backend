package com.mib.backend.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        String category,
        String status,
        LocalDate dueDate,
        Instant completedAt,
        Instant createdAt
) {
}
