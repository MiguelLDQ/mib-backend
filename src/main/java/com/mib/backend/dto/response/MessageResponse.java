package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID chatRoomId,
        UUID senderId,
        String senderUsername,
        String senderDisplayName,
        String senderAvatarUrl,
        String senderEquippedTitle,
        String senderEquippedFrameUrl,
        String content,
        Instant createdAt
) {
}
