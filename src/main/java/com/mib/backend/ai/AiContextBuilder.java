package com.mib.backend.ai;

import com.mib.backend.entity.MoodLevel;
import com.mib.backend.entity.User;
import com.mib.backend.repository.MoodRepository;
import com.mib.backend.repository.UserAchievementRepository;
import com.mib.backend.repository.UserInterestRepository;
import com.mib.backend.util.LevelCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Constroi o resumo de contexto do usuario ("memoria") injetado no prompt da IA:
 * nivel/XP, streak de acesso, interesses, conquistas recentes e uma leitura agregada
 * do humor dos ultimos 7 dias.
 * <p>
 * Decisao deliberada de privacidade: o resumo inclui apenas os NIVEIS de humor
 * (ex.: "2 dias bem, 1 dia triste"), nunca as notas de texto livre que o usuario
 * escreveu no diario — essas notas podem conter desabafos sensiveis que a pessoa
 * escreveu para si mesma, nao para serem repassados a um servico de IA de terceiros
 * a cada mensagem.
 */
@Component
@RequiredArgsConstructor
public class AiContextBuilder {

    private final UserInterestRepository userInterestRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final MoodRepository moodRepository;

    public String build(User user) {
        StringBuilder context = new StringBuilder();
        context.append("Contexto do usuario (use com naturalidade, nunca liste estes dados literalmente):\n");

        String displayName = user.getProfile() != null && user.getProfile().getDisplayName() != null
                ? user.getProfile().getDisplayName() : user.getUsername();
        context.append("- Nome: ").append(displayName).append("\n");

        LevelCalculator.LevelProgress progress = LevelCalculator.calculate(user.getTotalXp());
        context.append("- Nivel ").append(progress.level())
                .append(", ").append(user.getCurrentLoginStreak()).append(" dia(s) seguido(s) de acesso ao app\n");

        var interests = userInterestRepository.findAllByUserId(user.getId()).stream()
                .map(ui -> ui.getInterest().getName())
                .limit(5)
                .collect(Collectors.joining(", "));
        if (!interests.isBlank()) {
            context.append("- Interesses: ").append(interests).append("\n");
        }

        var recentAchievements = userAchievementRepository.findAllByUserIdOrderByUnlockedAtDesc(user.getId()).stream()
                .limit(3)
                .map(ua -> ua.getAchievement().getName())
                .collect(Collectors.joining(", "));
        if (!recentAchievements.isBlank()) {
            context.append("- Conquistas recentes: ").append(recentAchievements).append("\n");
        }

        String moodSummary = buildMoodSummary(user.getId());
        if (moodSummary != null) {
            context.append("- Humor nos ultimos 7 dias: ").append(moodSummary).append("\n");
        }

        return context.toString();
    }

    private String buildMoodSummary(java.util.UUID userId) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        var moods = moodRepository.findByUserIdAndMoodDateBetweenOrderByMoodDateAsc(userId, start, end);
        if (moods.isEmpty()) {
            return null;
        }

        Map<MoodLevel, Long> counts = moods.stream()
                .collect(Collectors.groupingBy(m -> m.getMoodLevel(), () -> new EnumMap<>(MoodLevel.class), Collectors.counting()));

        return counts.entrySet().stream()
                .map(e -> e.getValue() + " dia(s) " + describeMood(e.getKey()))
                .collect(Collectors.joining(", "));
    }

    private String describeMood(MoodLevel level) {
        return switch (level) {
            case VERY_GOOD -> "muito bem";
            case GOOD -> "bem";
            case NEUTRAL -> "neutro";
            case SAD -> "triste";
            case VERY_SAD -> "muito triste";
        };
    }
}
