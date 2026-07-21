package com.mib.backend.repository;

import com.mib.backend.entity.MissionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MissionTemplateRepository extends JpaRepository<MissionTemplate, UUID> {

    List<MissionTemplate> findAllByActiveTrue();
}
