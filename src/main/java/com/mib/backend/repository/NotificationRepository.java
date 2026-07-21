package com.mib.backend.repository;

import com.mib.backend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndReadFalse(UUID userId);

    @Modifying
    @Transactional
    @Query("update Notification n set n.read = true where n.user.id = :userId and n.read = false")
    void markAllAsRead(@Param("userId") UUID userId);
}
