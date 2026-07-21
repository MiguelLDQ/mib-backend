package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        String targetType,
        UUID targetId,
        String reason,
        String description,
        String status,
        Instant createdAt
) {
}
