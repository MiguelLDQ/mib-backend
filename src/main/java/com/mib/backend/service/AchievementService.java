package com.mib.backend.service;

import com.mib.backend.dto.response.AchievementResponse;
import com.mib.backend.entity.User;

import java.util.List;
import java.util.UUID;

public interface AchievementService {

    List<AchievementResponse> listForUser(UUID userId);

    /** Concede a conquista de codigo informado ao usuario, se ele ainda nao a possuir.
     * Idempotente: chamar varias vezes com a mesma conquista nao concede XP duplicado. */
    void grant(User user, String achievementCode);

    void checkLoginStreak(User user, int currentStreak);

    void checkMessageMilestones(User user, long totalMessagesSent);
}
