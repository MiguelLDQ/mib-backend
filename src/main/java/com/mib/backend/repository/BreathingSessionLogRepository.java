package com.mib.backend.repository;

import com.mib.backend.entity.BreathingSessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BreathingSessionLogRepository extends JpaRepository<BreathingSessionLog, UUID> {

    long countByUserId(UUID userId);

    long countByUserIdAndXpAwardedTrueAndCreatedAtAfter(UUID userId, Instant since);

    @Query("select coalesce(sum(s.durationSeconds), 0) from BreathingSessionLog s where s.user.id = :userId")
    long sumDurationSecondsByUserId(@Param("userId") UUID userId);

    @Query("""
            select s.technique.id as techniqueId, s.technique.name as techniqueName, count(s) as sessionCount
            from BreathingSessionLog s
            where s.user.id = :userId
            group by s.technique.id, s.technique.name
            order by count(s) desc
            """)
    List<TechniqueUsage> countByUserGroupedByTechnique(@Param("userId") UUID userId);

    interface TechniqueUsage {
        UUID getTechniqueId();
        String getTechniqueName();
        long getSessionCount();
    }
}
