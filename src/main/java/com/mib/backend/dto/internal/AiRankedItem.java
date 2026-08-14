package com.mib.backend.dto.internal;

import java.util.UUID;

/**
 * Item individual retornado pela IA ao ranquear o catálogo de conteúdos
 * para um usuário específico. contentId DEVE corresponder a um item já
 * existente no catálogo (RecommendedContent) - a IA nunca cria itens novos.
 */
public record AiRankedItem(
        UUID contentId,
        String reason,
        double score
) {
}
