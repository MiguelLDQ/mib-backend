package com.mib.backend.dto.response;

import java.util.UUID;

public record InterestResponse(
        UUID id,
        String name,
        String iconName,
        boolean selectedByMe,
        UUID themeRoomId
) {
}