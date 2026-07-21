package com.mib.backend.repository;

import com.mib.backend.entity.AiMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AiMessageRepository extends JpaRepository<AiMessage, UUID> {

    Page<AiMessage> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<AiMessage> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);

    void deleteAllByUserId(UUID userId);
}
