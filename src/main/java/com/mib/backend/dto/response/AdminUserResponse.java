package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String username,
        String email,
        String displayName,
        int level,
        long totalXp,
        boolean enabled,
        boolean banned,
        Instant suspendedUntil,
        int warningCount,
        List<String> roles,
        Instant lastLoginAt,
        Instant createdAt
) {
}
