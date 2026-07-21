package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FriendSummary(
        UUID userId,
        String username,
        String displayName,
        String avatarUrl,
        String statusMessage,
        boolean online,
        Instant lastSeenAt,
        Instant friendsSince
) {
}
