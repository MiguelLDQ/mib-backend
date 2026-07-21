package com.mib.backend.repository;

import com.mib.backend.entity.ModerationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ModerationLogRepository extends JpaRepository<ModerationLog, UUID> {

    Page<ModerationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByUserId(UUID userId);
}
