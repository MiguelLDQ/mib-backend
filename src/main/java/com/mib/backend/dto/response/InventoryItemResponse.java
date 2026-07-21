package com.mib.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record InventoryItemResponse(
        UUID inventoryItemId,
        UUID shopItemId,
        String name,
        String description,
        String type,
        String iconUrl,
        String source,
        boolean equipped,
        Instant unlockedAt
) {
}
