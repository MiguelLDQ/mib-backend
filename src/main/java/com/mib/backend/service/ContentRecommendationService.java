package com.mib.backend.service;

import com.mib.backend.dto.response.ContentRecommendationResponse;

import java.util.List;
import java.util.UUID;

public interface ContentRecommendationService {

    /**
     * Retorna as recomendações do dia para o usuário. Se ainda não existirem,
     * calcula na hora (contexto + ranking via IA) e persiste.
     */
    List<ContentRecommendationResponse> getRecommendationsForUser(UUID userId);

    /** Força o recálculo das recomendações do dia, ignorando o cache. */
    List<ContentRecommendationResponse> regenerateRecommendations(UUID userId);

    /** Marca um item como clicado, usado como sinal de feedback. */
    void registerClick(UUID userId, UUID recommendationId);

    /** Marca um lote de recomendações como exibidas (para métricas de exposição). */
    void registerShown(UUID userId, List<UUID> recommendationIds);
}
