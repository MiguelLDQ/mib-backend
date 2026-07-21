package com.mib.backend.dto.response;

import java.util.UUID;

public record AdminShopItemResponse(
        UUID id,
        String name,
        String description,
        String type,
        int priceXp,
        String iconUrl,
        boolean active,
        boolean exclusiveToAchievement
) {
}
