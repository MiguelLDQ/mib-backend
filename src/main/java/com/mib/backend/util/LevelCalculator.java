package com.mib.backend.util;

/**
 * Calcula nivel e progresso a partir do XP total acumulado, usando a formula
 * combinada no projeto: XP necessario para avancar do nivel N para o N+1 = 100 * N^2.
 * O calculo e cumulativo: para chegar ao nivel N, e preciso ter acumulado a soma de
 * 100*i^2 para i de 1 ate N-1.
 */
public final class LevelCalculator {

    private static final int XP_MULTIPLIER = 100;

    private LevelCalculator() {
    }

    public static int xpRequiredForLevel(int level) {
        return XP_MULTIPLIER * level * level;
    }

    public static LevelProgress calculate(long totalXp) {
        int level = 1;
        long remaining = totalXp;

        while (remaining >= xpRequiredForLevel(level)) {
            remaining -= xpRequiredForLevel(level);
            level++;
        }

        int xpForNextLevel = xpRequiredForLevel(level);
        double progressPercentage = xpForNextLevel == 0 ? 0
                : Math.min(100.0, (remaining * 100.0) / xpForNextLevel);

        return new LevelProgress(level, totalXp, remaining, xpForNextLevel, progressPercentage);
    }

    public record LevelProgress(
            int level,
            long totalXp,
            long currentLevelXp,
            int xpForNextLevel,
            double progressPercentage
    ) {
    }
}
