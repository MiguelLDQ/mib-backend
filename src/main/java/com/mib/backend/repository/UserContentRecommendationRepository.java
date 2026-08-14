package com.mib.backend.repository;

import com.mib.backend.entity.UserContentRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface UserContentRecommendationRepository extends JpaRepository<UserContentRecommendation, UUID> {

    List<UserContentRecommendation> findByUserIdAndRecommendedForOrderByRelevanceScoreDesc(
            UUID userId, LocalDate recommendedFor);

    List<UserContentRecommendation> findByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByUserIdAndContentIdAndClickedTrue(UUID userId, UUID contentId);
}
