package com.mib.backend.service.impl;

import com.mib.backend.dto.response.NotificationResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.entity.Notification;
import com.mib.backend.entity.NotificationType;
import com.mib.backend.entity.User;
import com.mib.backend.exception.NotificationNotFoundException;
import com.mib.backend.repository.NotificationRepository;
import com.mib.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void notify(User user, NotificationType type, String title, String message,
                        String relatedType, UUID relatedId) {
        Notification saved = notificationRepository.save(
                new Notification(user, type, title, message, relatedType, relatedId));

        // Push em tempo real: so chega a quem estiver com uma sessao WebSocket ativa
        // (autenticada pelo WebSocketAuthInterceptor, que usa o userId como nome do
        // Principal — por isso o destino usa user.getId().toString() como identificador).
        messagingTemplate.convertAndSendToUser(
                user.getId().toString(), "/queue/notifications", toResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> list(UUID userId, boolean unreadOnly, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = unreadOnly
                ? notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return PagedResponse.from(result.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotificationNotFoundException("Notificacao nao encontrada"));

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType().name(), n.getTitle(), n.getMessage(),
                n.getRelatedType(), n.getRelatedId(), n.isRead(), n.getCreatedAt());
    }
}
