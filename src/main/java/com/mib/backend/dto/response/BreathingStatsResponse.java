package com.mib.backend.dto.response;

import java.util.List;

public record BreathingStatsResponse(
        long totalSessions,
        long totalMinutes,
        List<TechniqueUsageEntry> byTechnique
) {
    public record TechniqueUsageEntry(String techniqueName, long sessionCount) {
    }
}
