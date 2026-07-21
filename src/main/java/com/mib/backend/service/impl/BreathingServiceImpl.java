package com.mib.backend.service.impl;

import com.mib.backend.dto.request.CompleteBreathingSessionRequest;
import com.mib.backend.dto.response.BreathingStatsResponse;
import com.mib.backend.dto.response.BreathingTechniqueResponse;
import com.mib.backend.entity.BreathingSessionLog;
import com.mib.backend.entity.BreathingTechnique;
import com.mib.backend.entity.User;
import com.mib.backend.entity.XpReasonType;
import com.mib.backend.exception.BreathingTechniqueNotFoundException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.repository.BreathingSessionLogRepository;
import com.mib.backend.repository.BreathingTechniqueRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.AchievementCodes;
import com.mib.backend.service.AchievementService;
import com.mib.backend.service.BreathingService;
import com.mib.backend.service.XpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BreathingServiceImpl implements BreathingService {

    /** XP concedido por sessao concluida. */
    private static final int XP_PER_SESSION = 10;

    /** Limite diario de sessoes que rendem XP (a sessao ainda e registrada para
     * estatisticas mesmo apos o limite, so deixa de conceder XP extra). */
    private static final int MAX_XP_SESSIONS_PER_DAY = 5;

    private final BreathingTechniqueRepository techniqueRepository;
    private final BreathingSessionLogRepository sessionLogRepository;
    private final UserRepository userRepository;
    private final XpService xpService;
    private final AchievementService achievementService;

    @Override
    @Transactional(readOnly = true)
    public List<BreathingTechniqueResponse> listTechniques() {
        return techniqueRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .map(t -> new BreathingTechniqueResponse(
                        t.getId(), t.getCode(), t.getName(), t.getDescription(), t.getBenefits(),
                        t.getInhaleSeconds(), t.getHoldAfterInhaleSeconds(), t.getExhaleSeconds(),
                        t.getHoldAfterExhaleSeconds(), t.getSuggestedCycles()))
                .toList();
    }

    @Override
    @Transactional
    public void completeSession(UUID currentUserId, UUID techniqueId, CompleteBreathingSessionRequest request) {
        BreathingTechnique technique = techniqueRepository.findById(techniqueId)
                .filter(BreathingTechnique::isActive)
                .orElseThrow(() -> new BreathingTechniqueNotFoundException("Tecnica de respiracao nao encontrada"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        BreathingSessionLog log = new BreathingSessionLog(user, technique, request.durationSeconds());

        Instant startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        long xpSessionsToday = sessionLogRepository.countByUserIdAndXpAwardedTrueAndCreatedAtAfter(currentUserId, startOfToday);

        if (xpSessionsToday < MAX_XP_SESSIONS_PER_DAY) {
            log.setXpAwarded(true);
            xpService.awardXp(user, XP_PER_SESSION, XpReasonType.BREATHING_EXERCISE_COMPLETED,
                    "Sessao de respiracao: " + technique.getName());
        }

        sessionLogRepository.save(log);

        achievementService.grant(user, AchievementCodes.FIRST_BREATHING_EXERCISE);
    }

    @Override
    @Transactional(readOnly = true)
    public BreathingStatsResponse getStats(UUID currentUserId) {
        long totalSessions = sessionLogRepository.countByUserId(currentUserId);
        long totalSeconds = sessionLogRepository.sumDurationSecondsByUserId(currentUserId);

        var byTechnique = sessionLogRepository.countByUserGroupedByTechnique(currentUserId).stream()
                .map(row -> new BreathingStatsResponse.TechniqueUsageEntry(row.getTechniqueName(), row.getSessionCount()))
                .toList();

        return new BreathingStatsResponse(totalSessions, totalSeconds / 60, byTechnique);
    }
}
