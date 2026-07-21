package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
        UUID userId,
        String username,
        String displayName,
        String bio,
        String avatarUrl,
        String statusMessage,
        boolean isPublic,
        boolean online,
        Instant lastSeenAt,
        Instant memberSince,
        List<EquippedItemSummary> equippedItems
) {
}
