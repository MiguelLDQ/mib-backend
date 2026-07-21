package com.mib.backend.repository;

import com.mib.backend.entity.Mood;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MoodRepository extends JpaRepository<Mood, UUID> {

    Optional<Mood> findByUserIdAndMoodDate(UUID userId, LocalDate moodDate);

    List<Mood> findByUserIdAndMoodDateBetweenOrderByMoodDateAsc(UUID userId, LocalDate start, LocalDate end);
}
