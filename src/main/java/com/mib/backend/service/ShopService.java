package com.mib.backend.service;

import com.mib.backend.dto.response.InventoryItemResponse;
import com.mib.backend.dto.response.ShopItemResponse;

import java.util.List;
import java.util.UUID;

public interface ShopService {

    List<ShopItemResponse> listAvailableItems(UUID currentUserId);

    InventoryItemResponse purchase(UUID currentUserId, UUID shopItemId);
}
