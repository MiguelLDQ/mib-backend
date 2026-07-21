package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AiMessageResponse(
        UUID id,
        String role,
        String content,
        Instant createdAt
) {
}
