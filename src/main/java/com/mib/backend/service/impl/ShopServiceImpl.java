package com.mib.backend.service.impl;

import com.mib.backend.dto.response.InventoryItemResponse;
import com.mib.backend.dto.response.ShopItemResponse;
import com.mib.backend.entity.InventorySource;
import com.mib.backend.entity.NotificationType;
import com.mib.backend.entity.ShopItem;
import com.mib.backend.entity.User;
import com.mib.backend.entity.UserInventoryItem;
import com.mib.backend.exception.InsufficientXpException;
import com.mib.backend.exception.ItemAlreadyOwnedException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.exception.ShopItemNotFoundException;
import com.mib.backend.repository.ShopItemRepository;
import com.mib.backend.repository.UserInventoryItemRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.NotificationService;
import com.mib.backend.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopItemRepository shopItemRepository;
    private final UserInventoryItemRepository inventoryItemRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<ShopItemResponse> listAvailableItems(UUID currentUserId) {
        return shopItemRepository.findAllByActiveTrueAndExclusiveToAchievementFalseOrderByTypeAscPriceXpAsc().stream()
                .map(item -> new ShopItemResponse(
                        item.getId(), item.getName(), item.getDescription(), item.getType().name(),
                        item.getPriceXp(), item.getIconUrl(),
                        inventoryItemRepository.existsByUserIdAndShopItemId(currentUserId, item.getId())))
                .toList();
    }

    @Override
    @Transactional
    public InventoryItemResponse purchase(UUID currentUserId, UUID shopItemId) {
        ShopItem item = shopItemRepository.findById(shopItemId)
                .filter(ShopItem::isActive)
                .orElseThrow(() -> new ShopItemNotFoundException("Item nao encontrado"));

        if (item.isExclusiveToAchievement()) {
            throw new ShopItemNotFoundException("Este item so pode ser obtido atraves de uma conquista");
        }

        if (inventoryItemRepository.existsByUserIdAndShopItemId(currentUserId, shopItemId)) {
            throw new ItemAlreadyOwnedException("Voce ja possui este item");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        if (user.getXpWallet() < item.getPriceXp()) {
            throw new InsufficientXpException("XP insuficiente para comprar este item");
        }

        user.setXpWallet(user.getXpWallet() - item.getPriceXp());
        userRepository.save(user);

        UserInventoryItem saved = inventoryItemRepository.save(
                new UserInventoryItem(user, item, InventorySource.PURCHASE));

        notificationService.notify(user, NotificationType.SHOP_PURCHASE,
                "Compra realizada",
                "Voce adquiriu \"" + item.getName() + "\" por " + item.getPriceXp() + " XP",
                "SHOP_ITEM", item.getId());

        return new InventoryItemResponse(
                saved.getId(), item.getId(), item.getName(), item.getDescription(), item.getType().name(),
                item.getIconUrl(), saved.getSource().name(), saved.isEquipped(), saved.getUnlockedAt());
    }
}
