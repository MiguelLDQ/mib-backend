package com.mib.backend.entity;

/**
 * Motivos de ganho de XP. Alguns valores sao reservados para modulos ainda nao
 * implementados (tasks, breathing, chat, community) e serao usados a partir das
 * fases seguintes, mantendo o historico de XP pronto para recebe-los sem migracao.
 */
public enum XpReasonType {
    DAILY_LOGIN,
    MISSION_COMPLETED,
    TASK_COMPLETED,
    BREATHING_EXERCISE_COMPLETED,
    POSITIVE_MESSAGE_SENT,
    LIKE_RECEIVED,
    FRIEND_HELPED,
    COMMUNITY_PARTICIPATION,
    ACHIEVEMENT_UNLOCKED,
    OTHER
}
