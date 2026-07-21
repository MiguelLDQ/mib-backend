package com.mib.backend.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MoodResponse(
        UUID id,
        String moodLevel,
        String note,
        LocalDate moodDate,
        Instant createdAt
) {
}
