package com.mib.backend.repository;

import com.mib.backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByChatRoomIdAndRemovedByModerationFalseOrderByCreatedAtDesc(UUID chatRoomId, Pageable pageable);

    long countBySenderIdAndRemovedByModerationFalse(UUID senderId);

    long countByRemovedByModerationFalse();
}
