package com.mib.backend.service;

import com.mib.backend.dto.request.RecordMoodRequest;
import com.mib.backend.dto.response.MoodResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MoodService {

    /** Registra o humor de hoje, ou atualiza o registro existente se ja houver um para o dia. */
    MoodResponse recordToday(UUID currentUserId, RecordMoodRequest request);

    Optional<MoodResponse> getToday(UUID currentUserId);

    List<MoodResponse> getHistory(UUID currentUserId, int days);
}
