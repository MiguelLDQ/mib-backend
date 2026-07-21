package com.mib.backend.service.impl;

import com.mib.backend.dto.response.AchievementResponse;
import com.mib.backend.entity.Achievement;
import com.mib.backend.entity.InventorySource;
import com.mib.backend.entity.NotificationType;
import com.mib.backend.entity.User;
import com.mib.backend.entity.UserAchievement;
import com.mib.backend.entity.UserInventoryItem;
import com.mib.backend.entity.XpReasonType;
import com.mib.backend.event.LevelUpEvent;
import com.mib.backend.repository.AchievementRepository;
import com.mib.backend.repository.UserAchievementRepository;
import com.mib.backend.repository.UserInventoryItemRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.AchievementCodes;
import com.mib.backend.service.AchievementService;
import com.mib.backend.service.NotificationService;
import com.mib.backend.service.XpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserInventoryItemRepository inventoryItemRepository;
    private final UserRepository userRepository;
    private final XpService xpService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponse> listForUser(UUID userId) {
        var unlocked = userAchievementRepository.findAllByUserIdOrderByUnlockedAtDesc(userId);
        var unlockedByCode = unlocked.stream()
                .collect(java.util.stream.Collectors.toMap(ua -> ua.getAchievement().getCode(), ua -> ua));

        return achievementRepository.findAllByOrderByXpRewardAsc().stream()
                .map(achievement -> {
                    UserAchievement ua = unlockedByCode.get(achievement.getCode());
                    return new AchievementResponse(
                            achievement.getId(),
                            achievement.getCode(),
                            achievement.getName(),
                            achievement.getDescription(),
                            achievement.getXpReward(),
                            achievement.getRewardShopItem() != null ? achievement.getRewardShopItem().getName() : null,
                            ua != null,
                            ua != null ? ua.getUnlockedAt() : null
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public void grant(User user, String achievementCode) {
        if (userAchievementRepository.existsByUserIdAndAchievementCode(user.getId(), achievementCode)) {
            return; // ja desbloqueada: operacao idempotente, nada a fazer
        }

        Achievement achievement = achievementRepository.findByCode(achievementCode).orElse(null);
        if (achievement == null) {
            log.warn("Conquista com codigo {} nao encontrada; nada foi concedido", achievementCode);
            return;
        }

        userAchievementRepository.save(new UserAchievement(user, achievement));

        if (achievement.getXpReward() > 0) {
            xpService.awardXp(user, achievement.getXpReward(), XpReasonType.ACHIEVEMENT_UNLOCKED,
                    "Conquista desbloqueada: " + achievement.getName());
        }

        if (achievement.getRewardShopItem() != null
                && !inventoryItemRepository.existsByUserIdAndShopItemId(user.getId(), achievement.getRewardShopItem().getId())) {
            inventoryItemRepository.save(new UserInventoryItem(user, achievement.getRewardShopItem(), InventorySource.ACHIEVEMENT));
        }

        log.info("Usuario {} desbloqueou a conquista {}", user.getId(), achievementCode);

        notificationService.notify(user, NotificationType.ACHIEVEMENT_UNLOCKED,
                "Nova conquista desbloqueada",
                "Voce desbloqueou \"" + achievement.getName() + "\"",
                "ACHIEVEMENT", achievement.getId());
    }

    @Override
    @Transactional
    public void checkLoginStreak(User user, int currentStreak) {
        if (currentStreak >= 7) {
            grant(user, AchievementCodes.STREAK_7_DAYS);
        }
        if (currentStreak >= 30) {
            grant(user, AchievementCodes.STREAK_30_DAYS);
        }
    }

    @Override
    @Transactional
    public void checkMessageMilestones(User user, long totalMessagesSent) {
        if (totalMessagesSent >= 100) {
            grant(user, AchievementCodes.MESSAGES_100);
        }
    }

    @EventListener
    @Transactional
    public void onLevelUp(LevelUpEvent event) {
        userRepository.findById(event.userId()).ifPresent(user -> {
            notificationService.notify(user, NotificationType.LEVEL_UP,
                    "Voce subiu de nivel!",
                    "Parabens, voce alcancou o nivel " + event.newLevel(),
                    "USER_LEVEL", null);

            if (event.newLevel() >= 5) {
                grant(user, AchievementCodes.LEVEL_5);
            }
            if (event.newLevel() >= 10) {
                grant(user, AchievementCodes.LEVEL_10);
            }
            if (event.newLevel() >= 20) {
                grant(user, AchievementCodes.LEVEL_20);
            }
        });
    }
}
