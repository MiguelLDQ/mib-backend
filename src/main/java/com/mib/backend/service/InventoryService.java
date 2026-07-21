package com.mib.backend.service;

import com.mib.backend.dto.response.InventoryItemResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    List<InventoryItemResponse> listOwnInventory(UUID currentUserId);

    InventoryItemResponse equip(UUID currentUserId, UUID shopItemId);

    InventoryItemResponse unequip(UUID currentUserId, UUID shopItemId);
}
