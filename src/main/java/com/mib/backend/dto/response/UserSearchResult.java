package com.mib.backend.dto.response;

import java.util.UUID;

public record UserSearchResult(
        UUID userId,
        String username,
        String displayName,
        String avatarUrl,
        boolean online,
        String friendshipStatus // NONE, PENDING_SENT, PENDING_RECEIVED, FRIENDS
) {
}
