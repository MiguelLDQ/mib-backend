package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String title,
        String message,
        String relatedType,
        UUID relatedId,
        boolean read,
        Instant createdAt
) {
}
