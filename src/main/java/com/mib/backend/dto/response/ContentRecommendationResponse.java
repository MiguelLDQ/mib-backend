package com.mib.backend.dto.response;

import com.mib.backend.entity.ContentCategory;
import com.mib.backend.entity.ContentType;

import java.util.UUID;

public record ContentRecommendationResponse(
        UUID id,
        UUID contentId,
        String title,
        String description,
        String url,
        String thumbnailUrl,
        ContentType contentType,
        ContentCategory category,
        Integer durationMinutes,
        String source,
        String aiReason,
        double relevanceScore
) {
}
