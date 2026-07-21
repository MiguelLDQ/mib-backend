package com.mib.backend.service;

import com.mib.backend.dto.request.CompleteBreathingSessionRequest;
import com.mib.backend.dto.response.BreathingStatsResponse;
import com.mib.backend.dto.response.BreathingTechniqueResponse;

import java.util.List;
import java.util.UUID;

public interface BreathingService {

    List<BreathingTechniqueResponse> listTechniques();

    void completeSession(UUID currentUserId, UUID techniqueId, CompleteBreathingSessionRequest request);

    BreathingStatsResponse getStats(UUID currentUserId);
}
