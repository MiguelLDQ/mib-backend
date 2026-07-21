package com.mib.backend.dto.response;

import java.util.UUID;

public record ShopItemResponse(
        UUID id,
        String name,
        String description,
        String type,
        int priceXp,
        String iconUrl,
        boolean owned
) {
}
