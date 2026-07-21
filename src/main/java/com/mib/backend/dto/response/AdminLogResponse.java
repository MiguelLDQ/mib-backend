package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AdminLogResponse(
        UUID id,
        String adminUsername,
        String action,
        String targetType,
        UUID targetId,
        String detail,
        Instant createdAt
) {
}
