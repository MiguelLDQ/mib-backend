package com.mib.backend.dto.response;

public record PlatformStatsResponse(
        long totalUsers,
        long bannedUsers,
        long currentlySuspendedUsers,
        long newUsersLast7Days,
        long newUsersLast30Days,
        long totalChatMessages,
        long totalAnonymousMessages,
        long totalTasksCompleted,
        long totalMissionsCompleted,
        long totalBreathingSessions,
        long pendingReports
) {
}
