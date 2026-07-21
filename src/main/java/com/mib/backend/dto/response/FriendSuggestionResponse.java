package com.mib.backend.dto.response;

import java.util.UUID;

public record FriendSuggestionResponse(
        UUID userId,
        String username,
        String displayName,
        String avatarUrl,
        boolean online,
        long sharedInterestsCount,
        String friendshipStatus
) {
}
