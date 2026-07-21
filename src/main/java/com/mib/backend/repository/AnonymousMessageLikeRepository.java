package com.mib.backend.repository;

import com.mib.backend.entity.AnonymousMessageLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AnonymousMessageLikeRepository extends JpaRepository<AnonymousMessageLike, UUID> {

    Optional<AnonymousMessageLike> findByMessageIdAndUserId(UUID messageId, UUID userId);

    boolean existsByMessageIdAndUserId(UUID messageId, UUID userId);
}
