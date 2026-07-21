package com.mib.backend.dto.response;

import java.util.UUID;

public record DirectRoomResponse(
        UUID roomId,
        UUID friendUserId,
        String friendUsername,
        String friendDisplayName,
        String friendAvatarUrl,
        boolean friendOnline
) {
}
