package com.mib.backend.repository;

import com.mib.backend.entity.UserMissionCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserMissionCompletionRepository extends JpaRepository<UserMissionCompletion, UUID> {

    Optional<UserMissionCompletion> findByUserIdAndDailyMissionId(UUID userId, UUID dailyMissionId);

    @org.springframework.data.jpa.repository.Query("""
            select c from UserMissionCompletion c
            where c.user.id = :userId and c.dailyMission.missionDate = :date
            """)
    List<UserMissionCompletion> findAllByUserIdAndMissionDate(@org.springframework.data.repository.query.Param("userId") UUID userId,
                                                                @org.springframework.data.repository.query.Param("date") LocalDate date);
}
