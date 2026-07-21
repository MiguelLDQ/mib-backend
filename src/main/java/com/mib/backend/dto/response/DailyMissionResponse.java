package com.mib.backend.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DailyMissionResponse(
        UUID missionId,
        String title,
        String description,
        String category,
        String difficulty,
        int xpReward,
        LocalDate missionDate,
        boolean completed,
        Instant completedAt
) {
}
