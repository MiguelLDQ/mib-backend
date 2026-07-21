package com.mib.backend.service;

import com.mib.backend.dto.response.DailyMissionResponse;

import java.util.List;
import java.util.UUID;

public interface MissionService {

    List<DailyMissionResponse> getTodayMissions(UUID currentUserId);

    void completeMission(UUID currentUserId, UUID dailyMissionId);

    /** Gera as missoes do dia caso ainda nao existam. Chamado pelo scheduler e, como
     * salvaguarda, tambem sob demanda caso o servidor tenha ficado fora do ar a meia-noite.
     * @return true se novas missoes foram geradas agora (false se ja existiam ou nao havia templates). */
    boolean ensureTodayMissionsGenerated();
}
