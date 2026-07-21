package com.mib.backend.dto.response;

import java.util.UUID;

public record PositiveFeedItemResponse(
        UUID id,
        String type,
        String content,
        String author
) {
}
