package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AnonymousMessageResponse(
        UUID id,
        UUID parentMessageId,
        String content,
        int likeCount,
        int replyCount,
        boolean likedByMe,
        boolean isMine,
        Instant createdAt
) {
}
