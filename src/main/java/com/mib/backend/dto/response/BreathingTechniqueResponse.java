package com.mib.backend.dto.response;

import java.util.UUID;

public record BreathingTechniqueResponse(
        UUID id,
        String code,
        String name,
        String description,
        String benefits,
        int inhaleSeconds,
        int holdAfterInhaleSeconds,
        int exhaleSeconds,
        int holdAfterExhaleSeconds,
        int suggestedCycles
) {
}
