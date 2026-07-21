package com.mib.backend.event;

import java.util.UUID;

/**
 * Publicado quando um usuario sobe de nivel. Usado para desacoplar o XpService do
 * AchievementService (que por sua vez depende do XpService para conceder XP de
 * recompensa) — evitando uma dependencia circular entre os dois beans.
 */
public record LevelUpEvent(UUID userId, int newLevel) {
}
