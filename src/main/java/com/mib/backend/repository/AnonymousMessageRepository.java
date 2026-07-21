package com.mib.backend.repository;

import com.mib.backend.entity.AnonymousMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnonymousMessageRepository extends JpaRepository<AnonymousMessage, UUID> {

    Page<AnonymousMessage> findByParentMessageIsNullAndRemovedByModerationFalseAndRemovedByAuthorFalseOrderByCreatedAtDesc(
            Pageable pageable);

    List<AnonymousMessage> findByParentMessageIdAndRemovedByModerationFalseAndRemovedByAuthorFalseOrderByCreatedAtAsc(
            UUID parentMessageId);

    long countByRemovedByModerationFalseAndRemovedByAuthorFalse();
}
