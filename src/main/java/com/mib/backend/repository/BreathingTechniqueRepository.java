package com.mib.backend.repository;

import com.mib.backend.entity.BreathingTechnique;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BreathingTechniqueRepository extends JpaRepository<BreathingTechnique, UUID> {

    List<BreathingTechnique> findAllByActiveTrueOrderByNameAsc();
}
