package com.mib.backend.service.impl;

import com.mib.backend.dto.response.DailyMissionResponse;
import com.mib.backend.entity.DailyMission;
import com.mib.backend.entity.MissionTemplate;
import com.mib.backend.entity.NotificationType;
import com.mib.backend.entity.User;
import com.mib.backend.entity.UserMissionCompletion;
import com.mib.backend.entity.XpReasonType;
import com.mib.backend.exception.MissionAlreadyCompletedException;
import com.mib.backend.exception.MissionNotFoundException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.repository.DailyMissionRepository;
import com.mib.backend.repository.MissionTemplateRepository;
import com.mib.backend.repository.UserMissionCompletionRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.AchievementCodes;
import com.mib.backend.service.AchievementService;
import com.mib.backend.service.MissionService;
import com.mib.backend.service.NotificationService;
import com.mib.backend.service.XpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionServiceImpl implements MissionService {

    /** Quantidade de missoes sorteadas para compor o dia. */
    private static final int MISSIONS_PER_DAY = 5;

    private final MissionTemplateRepository missionTemplateRepository;
    private final DailyMissionRepository dailyMissionRepository;
    private final UserMissionCompletionRepository completionRepository;
    private final UserRepository userRepository;
    private final XpService xpService;
    private final AchievementService achievementService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<DailyMissionResponse> getTodayMissions(UUID currentUserId) {
        LocalDate today = LocalDate.now();
        List<DailyMission> missions = dailyMissionRepository.findAllByMissionDate(today);

        Map<UUID, UserMissionCompletion> completions = completionRepository
                .findAllByUserIdAndMissionDate(currentUserId, today).stream()
                .collect(Collectors.toMap(c -> c.getDailyMission().getId(), c -> c));

        return missions.stream()
                .map(mission -> {
                    UserMissionCompletion completion = completions.get(mission.getId());
                    MissionTemplate template = mission.getTemplate();
                    return new DailyMissionResponse(
                            mission.getId(),
                            template.getTitle(),
                            template.getDescription(),
                            template.getCategory().name(),
                            template.getDifficulty().name(),
                            mission.getXpReward(),
                            mission.getMissionDate(),
                            completion != null,
                            completion != null ? completion.getCompletedAt() : null
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public void completeMission(UUID currentUserId, UUID dailyMissionId) {
        DailyMission mission = dailyMissionRepository.findById(dailyMissionId)
                .orElseThrow(() -> new MissionNotFoundException("Missao nao encontrada"));

        if (!mission.getMissionDate().equals(LocalDate.now())) {
            throw new MissionNotFoundException("Esta missao nao esta mais disponivel");
        }

        completionRepository.findByUserIdAndDailyMissionId(currentUserId, dailyMissionId).ifPresent(c -> {
            throw new MissionAlreadyCompletedException("Voce ja concluiu esta missao hoje");
        });

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        completionRepository.save(new UserMissionCompletion(user, mission));

        xpService.awardXp(user, mission.getXpReward(), XpReasonType.MISSION_COMPLETED,
                "Missao concluida: " + mission.getTemplate().getTitle());

        achievementService.grant(user, AchievementCodes.FIRST_MISSION_COMPLETED);

        notificationService.notify(user, NotificationType.MISSION_COMPLETED,
                "Missao concluida",
                "Voce concluiu \"" + mission.getTemplate().getTitle() + "\" e ganhou " + mission.getXpReward() + " XP",
                "DAILY_MISSION", mission.getId());
    }

    @Override
    @Transactional
    public boolean ensureTodayMissionsGenerated() {
        LocalDate today = LocalDate.now();
        if (dailyMissionRepository.existsByMissionDate(today)) {
            return false;
        }

        List<MissionTemplate> activeTemplates = missionTemplateRepository.findAllByActiveTrue();
        if (activeTemplates.isEmpty()) {
            log.warn("Nenhum template de missao ativo encontrado; nenhuma missao gerada para {}", today);
            return false;
        }

        Collections.shuffle(activeTemplates);
        int count = Math.min(MISSIONS_PER_DAY, activeTemplates.size());

        activeTemplates.stream()
                .limit(count)
                .forEach(template -> dailyMissionRepository.save(
                        new DailyMission(template, today, template.getBaseXpReward())));

        log.info("Geradas {} missoes diarias para {}", count, today);
        return true;
    }
}
