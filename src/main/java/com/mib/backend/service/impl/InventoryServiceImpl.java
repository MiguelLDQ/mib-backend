package com.mib.backend.service.impl;

import com.mib.backend.dto.response.InventoryItemResponse;
import com.mib.backend.entity.ShopItemType;
import com.mib.backend.entity.UserInventoryItem;
import com.mib.backend.exception.ItemNotOwnedException;
import com.mib.backend.repository.UserInventoryItemRepository;
import com.mib.backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final UserInventoryItemRepository inventoryItemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> listOwnInventory(UUID currentUserId) {
        return inventoryItemRepository.findAllByUserIdOrderByUnlockedAtDesc(currentUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public InventoryItemResponse equip(UUID currentUserId, UUID shopItemId) {
        UserInventoryItem item = findOwnedItem(currentUserId, shopItemId);
        ShopItemType type = item.getShopItem().getType();

        // Emblemas (BADGE) podem ser equipados simultaneamente; os demais tipos sao
        // "slots" unicos — equipar um novo item do mesmo tipo desequipa o anterior.
        if (type != ShopItemType.BADGE) {
            inventoryItemRepository.findEquippedByUserIdAndType(currentUserId, type)
                    .forEach(equippedItem -> {
                        equippedItem.setEquipped(false);
                        inventoryItemRepository.save(equippedItem);
                    });
        }

        item.setEquipped(true);
        return toResponse(inventoryItemRepository.save(item));
    }

    @Override
    @Transactional
    public InventoryItemResponse unequip(UUID currentUserId, UUID shopItemId) {
        UserInventoryItem item = findOwnedItem(currentUserId, shopItemId);
        item.setEquipped(false);
        return toResponse(inventoryItemRepository.save(item));
    }

    private UserInventoryItem findOwnedItem(UUID currentUserId, UUID shopItemId) {
        return inventoryItemRepository.findByUserIdAndShopItemId(currentUserId, shopItemId)
                .orElseThrow(() -> new ItemNotOwnedException("Voce nao possui este item"));
    }

    private InventoryItemResponse toResponse(UserInventoryItem item) {
        var shopItem = item.getShopItem();
        return new InventoryItemResponse(
                item.getId(), shopItem.getId(), shopItem.getName(), shopItem.getDescription(),
                shopItem.getType().name(), shopItem.getIconUrl(), item.getSource().name(),
                item.isEquipped(), item.getUnlockedAt());
    }
}
