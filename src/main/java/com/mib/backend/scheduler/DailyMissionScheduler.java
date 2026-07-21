package com.mib.backend.scheduler;

import com.mib.backend.entity.NotificationType;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.MissionService;
import com.mib.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyMissionScheduler {

    private final MissionService missionService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /** Todos os dias a meia-noite (horario do servidor), gera o novo conjunto de missoes. */
    @Scheduled(cron = "0 0 0 * * *")
    public void generateDailyMissions() {
        if (missionService.ensureTodayMissionsGenerated()) {
            notifyAllUsersOfNewMissions();
        }
    }

    /** Salvaguarda: garante que existam missoes do dia assim que a aplicacao sobe,
     * cobrindo o caso do servidor ter ficado fora do ar exatamente a meia-noite. */
    @EventListener(ApplicationReadyEvent.class)
    public void generateOnStartup() {
        if (missionService.ensureTodayMissionsGenerated()) {
            notifyAllUsersOfNewMissions();
        }
    }

    /**
     * Notifica todos os usuarios ativos sobre as novas missoes do dia. Em escala maior,
     * este loop sincrono deveria virar um job assincrono/em fila (ex.: anotacao Async do
     * Spring, ou uma tabela de outbox) para nao prender a thread do scheduler — para o
     * volume de usuarios deste projeto, a abordagem direta e suficiente.
     */
    private void notifyAllUsersOfNewMissions() {
        var users = userRepository.findAll();
        users.forEach(user -> notificationService.notify(user, NotificationType.NEW_DAILY_MISSIONS,
                "Novas missoes disponiveis",
                "As missoes de hoje ja estao no ar. Vem conferir e ganhar XP!",
                null, null));

        log.info("Notificacao de novas missoes diarias enviada para {} usuarios", users.size());
    }
}
