package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ModerationLogResponse(
        UUID id,
        UUID userId,
        String username,
        String targetType,
        UUID targetId,
        String category,
        String action,
        String detail,
        Instant createdAt
) {
}
