package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FriendRequestResponse(
        UUID requestId,
        UUID userId,
        String username,
        String displayName,
        String avatarUrl,
        Instant requestedAt
) {
}
