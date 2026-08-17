package com.mib.backend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mib.backend.ai.GroqClient.GroqChatMessage;
import com.mib.backend.dto.AiRankedItem;
import com.mib.backend.dto.UserWellnessContext;
import com.mib.backend.entity.RecommendedContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Usa o GroqClient (já existente no projeto) exclusivamente para RANQUEAR
 * itens de um catálogo pré-existente. A IA nunca gera URLs/títulos novos:
 * isso elimina o risco de alucinação de links quebrados ou inexistentes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContentRankingAiClient {

    private static final String SYSTEM_PROMPT = """
            Você é um assistente de bem-estar mental dentro de um app de saúde emocional.
            Você receberá:
            1. O contexto recente de um usuário (humor, tendência, interesses, dificuldades, hábitos de respiração).
            2. Uma lista de conteúdos disponíveis no catálogo (id, título, categoria, tags, duração).

            Sua tarefa: escolher até 5 itens dessa lista que sejam mais relevantes para esse usuário agora,
            e explicar em 1 frase curta e acolhedora o motivo de cada recomendação.

            REGRAS OBRIGATÓRIAS:
            - Use APENAS os "id" fornecidos na lista de conteúdos. NUNCA invente um id novo.
            - NUNCA crie títulos, URLs ou descrições que não estejam na lista.
            - Se nenhum item for relevante, retorne uma lista vazia.
            - Responda SOMENTE com um JSON válido, sem texto adicional, no formato:
              [{"contentId": "uuid", "reason": "motivo curto", "score": 0.0}]
            - "score" vai de 0.0 a 1.0, sendo 1.0 o mais relevante.
            """;

    // Reaproveita o client já configurado no projeto (com.mib.backend.ai.GroqClient)
    private final GroqClient groqClient;
    private final ObjectMapper objectMapper;

    public List<AiRankedItem> rank(UserWellnessContext context, List<RecommendedContent> candidates) {
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        String userPrompt = buildUserPrompt(context, candidates);

        try {
            List<GroqChatMessage> messages = List.of(
                    new GroqChatMessage("system", SYSTEM_PROMPT),
                    new GroqChatMessage("user", userPrompt));

            String rawResponse = groqClient.chat(messages);
            String json = extractJson(rawResponse);
            List<AiRankedItem> ranked = objectMapper.readValue(
                    json, objectMapper.getTypeFactory().constructCollectionType(List.class, AiRankedItem.class));

            // Sanitização: descarta qualquer contentId que não exista de fato no catálogo enviado
            var validIds = candidates.stream().map(RecommendedContent::getId).collect(Collectors.toSet());
            return ranked.stream()
                    .filter(item -> validIds.contains(item.contentId()))
                    .toList();

        } catch (Exception e) {
            log.warn("Falha ao ranquear conteúdo via IA, aplicando fallback por regras. Motivo: {}", e.getMessage());
            return fallbackRanking(candidates);
        }
    }

    private String buildUserPrompt(UserWellnessContext context, List<RecommendedContent> candidates) {
        StringBuilder sb = new StringBuilder();
        sb.append("CONTEXTO DO USUÁRIO:\n");
        sb.append("- Humor dominante: ").append(context.dominantMood()).append("\n");
        sb.append("- Tendência de humor: ").append(context.moodTrend()).append("\n");
        sb.append("- Interesses: ").append(String.join(", ", context.interests())).append("\n");
        sb.append("- Categorias em que o usuário mais se engaja: ").append(context.engagedCategories()).append("\n");
        sb.append("- Sessões de respiração (14 dias): ").append(context.breathingSessionsLast14Days()).append("\n");
        sb.append("- Técnicas de respiração preferidas: ")
                .append(String.join(", ", context.preferredBreathingTechniques())).append("\n");
        sb.append("- Streak atual: ").append(context.currentStreakDays()).append(" dias\n\n");

        sb.append("CATÁLOGO DISPONÍVEL:\n");
        for (RecommendedContent c : candidates) {
            sb.append("- id: ").append(c.getId())
                    .append(" | título: ").append(c.getTitle())
                    .append(" | tipo: ").append(c.getContentType())
                    .append(" | categoria: ").append(c.getCategory())
                    .append(" | duração: ").append(c.getDurationMinutes()).append("min")
                    .append(" | tags: ").append(c.getTags())
                    .append("\n");
        }
        return sb.toString();
    }

    /** Fallback simples baseado em regras, caso a IA falhe ou retorne algo inválido. */
    private List<AiRankedItem> fallbackRanking(List<RecommendedContent> candidates) {
        return candidates.stream()
                .limit(5)
                .map(c -> new AiRankedItem(c.getId(), "Selecionado com base nas suas categorias recentes.", 0.5))
                .toList();
    }

    private String extractJson(String rawResponse) {
        int start = rawResponse.indexOf('[');
        int end = rawResponse.lastIndexOf(']');
        if (start == -1 || end == -1 || end < start) {
            throw new IllegalStateException("Resposta da IA não contém um JSON de array válido");
        }
        return rawResponse.substring(start, end + 1);
    }
}
