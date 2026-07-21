package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AchievementResponse(
        UUID id,
        String code,
        String name,
        String description,
        int xpReward,
        String rewardItemName,
        boolean unlocked,
        Instant unlockedAt
) {
}
