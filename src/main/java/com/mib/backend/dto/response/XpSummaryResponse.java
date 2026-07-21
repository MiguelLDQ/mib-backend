package com.mib.backend.dto.response;

public record XpSummaryResponse(
        long totalXp,
        int level,
        long currentLevelXp,
        int xpForNextLevel,
        double progressPercentage,
        long xpWallet
) {
}
