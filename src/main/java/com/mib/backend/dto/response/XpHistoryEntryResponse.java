package com.mib.backend.dto.response;

import java.time.Instant;

public record XpHistoryEntryResponse(
        int amount,
        String reason,
        String description,
        int levelAfter,
        Instant createdAt
) {
}
