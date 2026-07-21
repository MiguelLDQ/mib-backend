package com.mib.backend.repository;

import com.mib.backend.entity.DailyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DailyMissionRepository extends JpaRepository<DailyMission, UUID> {

    List<DailyMission> findAllByMissionDate(LocalDate missionDate);

    boolean existsByMissionDate(LocalDate missionDate);
}
