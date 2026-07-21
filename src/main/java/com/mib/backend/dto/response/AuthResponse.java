package com.mib.backend.dto.response;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UserSummary user
) {
    public static AuthResponse of(String accessToken, String refreshToken, UserSummary user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", user);
    }

    public record UserSummary(
            UUID id,
            String username,
            String email,
            String displayName
    ) {
    }
}
