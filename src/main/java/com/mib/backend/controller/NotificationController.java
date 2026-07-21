package com.mib.backend.controller;

import com.mib.backend.dto.response.NotificationResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notificacoes do usuario (tambem entregues em tempo real via WebSocket)")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Lista notificacoes do usuario (paginado), com filtro opcional para so nao lidas")
    public ResponseEntity<PagedResponse<NotificationResponse>> list(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(notificationService.list(SecurityUtils.currentUserId(), unreadOnly, page, size));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Quantidade de notificacoes nao lidas (para o badge do app)")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(SecurityUtils.currentUserId())));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Marca uma notificacao como lida")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        notificationService.markAsRead(SecurityUtils.currentUserId(), notificationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Marca todas as notificacoes do usuario como lidas")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead(SecurityUtils.currentUserId());
        return ResponseEntity.noContent().build();
    }
}
