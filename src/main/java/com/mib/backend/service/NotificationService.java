package com.mib.backend.service;

import com.mib.backend.dto.response.NotificationResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.entity.NotificationType;
import com.mib.backend.entity.User;

import java.util.UUID;

public interface NotificationService {

    /** Cria a notificacao, persiste e envia em tempo real via WebSocket
     * (/user/{userId}/queue/notifications) para quem estiver conectado. */
    void notify(User user, NotificationType type, String title, String message,
                String relatedType, UUID relatedId);

    PagedResponse<NotificationResponse> list(UUID userId, boolean unreadOnly, int page, int size);

    long countUnread(UUID userId);

    void markAsRead(UUID userId, UUID notificationId);

    void markAllAsRead(UUID userId);
}
