package com.mib.backend.dto.internal;

import com.mib.backend.entity.ContentCategory;
import lombok.Builder;

import java.util.List;
import java.util.Set;

/**
 * Contexto agregado do usuário, calculado a partir de Mood, UserInterest,
 * UserMissionCompletion e BreathingSessionLog. É isso (e só isso) que vai
 * pro prompt da IA - nunca dados brutos sensíveis além do necessário.
 */
@Builder
public record UserWellnessContext(
        String dominantMood,
        String moodTrend, // "IMPROVING", "DECLINING", "STABLE"
        List<String> interests,
        Set<ContentCategory> strugglingCategories,
        int breathingSessionsLast14Days,
        List<String> preferredBreathingTechniques,
        int currentStreakDays
) {
}
