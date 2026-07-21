package com.mib.backend.repository;

import com.mib.backend.entity.XpHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface XpHistoryRepository extends JpaRepository<XpHistory, UUID> {

    Page<XpHistory> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
