package com.mib.backend.controller;

import com.mib.backend.dto.response.ContentRecommendationResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.ContentRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class ContentRecommendationController {

    private final ContentRecommendationService recommendationService;

    /** Retorna as recomendações do dia para o usuário logado (calcula se ainda não existirem). */
    @GetMapping("/content")
    public ResponseEntity<List<ContentRecommendationResponse>> getRecommendations() {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(recommendationService.getRecommendationsForUser(userId));
    }

    /** Força recalcular as recomendações do dia (ex: botão "atualizar" no front). */
    @PostMapping("/content/refresh")
    public ResponseEntity<List<ContentRecommendationResponse>> refreshRecommendations() {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(recommendationService.regenerateRecommendations(userId));
    }

    /** Sinal de feedback: usuário clicou no conteúdo recomendado. */
    @PostMapping("/content/{recommendationId}/click")
    public ResponseEntity<Void> registerClick(@PathVariable UUID recommendationId) {
        UUID userId = SecurityUtils.currentUserId();
        recommendationService.registerClick(userId, recommendationId);
        return ResponseEntity.noContent().build();
    }

    /** Sinal de exposição: usados para métricas de quantos itens foram realmente vistos. */
    @PostMapping("/content/shown")
    public ResponseEntity<Void> registerShown(@RequestBody List<UUID> recommendationIds) {
        UUID userId = SecurityUtils.currentUserId();
        recommendationService.registerShown(userId, recommendationIds);
        return ResponseEntity.noContent().build();
    }
}
