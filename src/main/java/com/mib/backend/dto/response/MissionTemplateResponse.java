package com.mib.backend.dto.response;

import java.util.UUID;

public record MissionTemplateResponse(
        UUID id,
        String title,
        String description,
        String category,
        String difficulty,
        int baseXpReward,
        boolean active
) {
}
