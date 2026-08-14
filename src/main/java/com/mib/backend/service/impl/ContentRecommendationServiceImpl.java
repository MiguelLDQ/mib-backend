package com.mib.backend.service.impl;

import com.mib.backend.ai.ContentRankingAiClient;
import com.mib.backend.dto.AiRankedItem;
import com.mib.backend.dto.UserWellnessContext;
import com.mib.backend.dto.response.ContentRecommendationResponse;
import com.mib.backend.entity.ContentCategory;
import com.mib.backend.entity.Mood;
import com.mib.backend.entity.MissionCategory;
import com.mib.backend.entity.RecommendedContent;
import com.mib.backend.entity.User;
import com.mib.backend.entity.UserContentRecommendation;
import com.mib.backend.entity.UserMissionCompletion;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.repository.BreathingSessionLogRepository;
import com.mib.backend.repository.MoodRepository;
import com.mib.backend.repository.RecommendedContentRepository;
import com.mib.backend.repository.UserContentRecommendationRepository;
import com.mib.backend.repository.UserInterestRepository;
import com.mib.backend.repository.UserMissionCompletionRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.ContentRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentRecommendationServiceImpl implements ContentRecommendationService {

    private static final int LOOKBACK_DAYS = 14;
    private static final int MAX_RECOMMENDATIONS = 5;

    private final MoodRepository moodRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserMissionCompletionRepository missionCompletionRepository;
    private final BreathingSessionLogRepository breathingSessionLogRepository;
    private final RecommendedContentRepository contentRepository;
    private final UserContentRecommendationRepository userContentRepository;
    private final UserRepository userRepository;
    private final ContentRankingAiClient rankingAiClient;

    @Override
    @Transactional(readOnly = true)
    public List<ContentRecommendationResponse> getRecommendationsForUser(UUID userId) {
        var cached = userContentRepository
                .findByUserIdAndRecommendedForOrderByRelevanceScoreDesc(userId, LocalDate.now());

        if (!cached.isEmpty()) {
            return toResponse(cached);
        }

        return generateAndPersist(userId);
    }

    @Override
    @Transactional
    public List<ContentRecommendationResponse> regenerateRecommendations(UUID userId) {
        return generateAndPersist(userId);
    }

    @Override
    @Transactional
    public void registerClick(UUID userId, UUID recommendationId) {
        var recommendation = userContentRepository.findById(recommendationId)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendação não encontrada"));

        if (!recommendation.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Recomendação não encontrada");
        }

        recommendation.setClicked(true);
        userContentRepository.save(recommendation);
    }

    @Override
    @Transactional
    public void registerShown(UUID userId, List<UUID> recommendationIds) {
        var recommendations = userContentRepository.findAllById(recommendationIds).stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .peek(r -> r.setShown(true))
                .toList();

        userContentRepository.saveAll(recommendations);
    }

    // ---------------------------------------------------------------------
    // Núcleo do algoritmo
    // ---------------------------------------------------------------------

    private List<ContentRecommendationResponse> generateAndPersist(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        UserWellnessContext context = buildContext(userId);

        Set<ContentCategory> candidateCategories = mapContextToCategories(context);
        List<RecommendedContent> candidates = contentRepository
                .findByCategoryInAndActiveTrue(candidateCategories);

        if (candidates.isEmpty()) {
            log.info("Nenhum conteúdo candidato encontrado para o usuário {}", userId);
            return List.of();
        }

        List<AiRankedItem> ranked = rankingAiClient.rank(context, candidates);

        if (ranked.isEmpty()) {
            log.info("IA não retornou recomendações válidas para o usuário {}", userId);
            return List.of();
        }

        Map<UUID, RecommendedContent> byId = candidates.stream()
                .collect(Collectors.toMap(RecommendedContent::getId, c -> c));

        List<UserContentRecommendation> toSave = ranked.stream()
                .limit(MAX_RECOMMENDATIONS)
                .filter(item -> byId.containsKey(item.contentId()))
                .map(item -> UserContentRecommendation.builder()
                        .user(user)
                        .content(byId.get(item.contentId()))
                        .relevanceScore(item.score())
                        .aiReason(item.reason())
                        .recommendedFor(LocalDate.now())
                        .shown(false)
                        .clicked(false)
                        .build())
                .toList();

        var saved = userContentRepository.saveAll(toSave);
        return toResponse(saved);
    }

    private UserWellnessContext buildContext(UUID userId) {
        Instant since = Instant.now().minusSeconds(LOOKBACK_DAYS * 24L * 60 * 60);

        List<Mood> recentMoods = moodRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since);
        List<String> interests = userInterestRepository.findByUserId(userId).stream()
                .map(ui -> ui.getInterest().getName())
                .toList();
        List<UserMissionCompletion> recentCompletions = missionCompletionRepository
                .findByUserIdAndCompletedAtAfter(userId, since);
        var breathingLogs = breathingSessionLogRepository.findByUserIdAndCreatedAtAfter(userId, since);

        return UserWellnessContext.builder()
                .dominantMood(computeDominantMood(recentMoods))
                .moodTrend(computeMoodTrend(recentMoods))
                .interests(interests)
                .engagedCategories(computeEngagedCategories(recentCompletions))
                .breathingSessionsLast14Days(breathingLogs.size())
                .preferredBreathingTechniques(
                        breathingLogs.stream()
                                .map(log -> log.getBreathingTechnique().getName())
                                .distinct()
                                .toList())
                .currentStreakDays(computeStreak(recentCompletions))
                .build();
    }

    /** Mapeia o contexto calculado para categorias de conteúdo plausíveis, antes de chamar a IA. */
    private Set<ContentCategory> mapContextToCategories(UserWellnessContext context) {
        Set<ContentCategory> categories = new HashSet<>(context.engagedCategories());

        if ("DECLINING".equals(context.moodTrend()) || "ANSIOSO".equalsIgnoreCase(context.dominantMood())) {
            categories.add(ContentCategory.ANXIETY);
            categories.add(ContentCategory.STRESS_RELIEF);
        }

        if (context.breathingSessionsLast14Days() > 0) {
            categories.add(ContentCategory.BREATHING);
        }

        if (context.currentStreakDays() == 0) {
            categories.add(ContentCategory.MOTIVATION);
        }

        // Sempre garante um mínimo de diversidade caso nada específico tenha sido identificado
        if (categories.isEmpty()) {
            categories.add(ContentCategory.GENERAL_WELLNESS);
            categories.add(ContentCategory.MINDFULNESS);
        }

        return categories;
    }

    private String computeDominantMood(List<Mood> moods) {
        if (moods.isEmpty()) {
            return "NEUTRO";
        }
        Map<String, Long> counts = new HashMap<>();
        for (Mood m : moods) {
            counts.merge(m.getMoodLevel().name(), 1L, Long::sum);
        }
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("NEUTRO");
    }

    private String computeMoodTrend(List<Mood> moods) {
        if (moods.size() < 2) {
            return "STABLE";
        }
        // Assume moods ordenados do mais recente para o mais antigo
        double recentAvg = averageMoodScore(moods.subList(0, moods.size() / 2));
        double olderAvg = averageMoodScore(moods.subList(moods.size() / 2, moods.size()));

        if (recentAvg > olderAvg + 0.5) return "IMPROVING";
        if (recentAvg < olderAvg - 0.5) return "DECLINING";
        return "STABLE";
    }

    private double averageMoodScore(List<Mood> moods) {
        return moods.stream()
                .mapToInt(m -> m.getMoodLevel().ordinal())
                .average()
                .orElse(0.0);
    }

    /**
     * Sinal POSITIVO de engajamento: como UserMissionCompletion só registra o que
     * de fato foi completado (não existe "missão não completada" nessa tabela),
     * não dá pra calcular taxa de conclusão por categoria. Em vez disso, olhamos
     * quais categorias de missão o usuário mais completou - é um proxy razoável
     * de interesse/hábito, e reforça recomendações de conteúdo na mesma linha.
     */
    private Set<ContentCategory> computeEngagedCategories(List<UserMissionCompletion> completions) {
        Map<MissionCategory, Long> counts = new HashMap<>();
        for (UserMissionCompletion completion : completions) {
            MissionCategory category = completion.getDailyMission().getTemplate().getCategory();
            counts.merge(category, 1L, Long::sum);
        }

        // Pega as categorias com pelo menos 2 conclusões no período, como sinal de hábito
        // (uma única conclusão isolada não indica engajamento de verdade).
        Set<ContentCategory> engaged = new HashSet<>();
        counts.forEach((category, count) -> {
            if (count >= 2) {
                mapMissionCategoryToContentCategory(category).ifPresent(engaged::add);
            }
        });
        return engaged;
    }

    /**
     * Mapeia MissionCategory (domínio de missões) para ContentCategory (domínio de
     * conteúdo recomendado). Tenta por nome primeiro; ajuste aqui se os enums não
     * tiverem valores com nomes correspondentes.
     */
    private Optional<ContentCategory> mapMissionCategoryToContentCategory(MissionCategory missionCategory) {
        try {
            return Optional.of(ContentCategory.valueOf(missionCategory.name()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private int computeStreak(List<UserMissionCompletion> completions) {
        return (int) completions.stream()
                .map(c -> c.getCompletedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate())
                .distinct()
                .count();
    }

    private List<ContentRecommendationResponse> toResponse(List<UserContentRecommendation> recommendations) {
        return recommendations.stream()
                .map(r -> new ContentRecommendationResponse(
                        r.getId(),
                        r.getContent().getId(),
                        r.getContent().getTitle(),
                        r.getContent().getDescription(),
                        r.getContent().getUrl(),
                        r.getContent().getThumbnailUrl(),
                        r.getContent().getContentType(),
                        r.getContent().getCategory(),
                        r.getContent().getDurationMinutes(),
                        r.getContent().getSource(),
                        r.getAiReason(),
                        r.getRelevanceScore()))
                .toList();
    }
}