package com.mib.backend.repository;

import com.mib.backend.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {

    boolean existsByUserIdAndAchievementCode(UUID userId, String achievementCode);

    List<UserAchievement> findAllByUserIdOrderByUnlockedAtDesc(UUID userId);
}
