package com.mib.backend.scheduler;

import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.ContentRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Pré-computa as recomendações de conteúdo de todos os usuários ativos
 * de madrugada, seguindo o mesmo padrão do DailyMissionScheduler.
 * Isso evita chamar a IA em tempo real na primeira requisição do usuário.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContentRecommendationScheduler {

    private final UserRepository userRepository;
    private final ContentRecommendationService recommendationService;

    // Roda todo dia às 4h da manhã, um pouco antes das missões diárias por exemplo
    @Scheduled(cron = "0 0 4 * * *")
    public void generateDailyRecommendations() {
        log.info("Iniciando geração diária de recomendações de conteúdo...");

        // "Ativo" = conta habilitada e não banida. Ajuste aqui se quiser também
        // filtrar por isCurrentlySuspended() ou por lastActiveAt recente.
        var activeUsers = userRepository.findByBannedFalseAndEnabledTrue();
        int success = 0;
        int failures = 0;

        for (var user : activeUsers) {
            try {
                recommendationService.regenerateRecommendations(user.getId());
                success++;
            } catch (Exception e) {
                failures++;
                log.error("Falha ao gerar recomendações para o usuário {}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("Geração diária de recomendações concluída. Sucesso: {}, Falhas: {}", success, failures);
    }
}
