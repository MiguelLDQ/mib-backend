package com.mib.backend.service.impl;

import com.mib.backend.dto.request.ShopItemRequest;
import com.mib.backend.dto.response.AdminShopItemResponse;
import com.mib.backend.entity.AdminActionType;
import com.mib.backend.entity.ShopItem;
import com.mib.backend.entity.ShopItemType;
import com.mib.backend.exception.BadRequestException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.exception.ShopItemNotFoundException;
import com.mib.backend.repository.ShopItemRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.AdminAuditService;
import com.mib.backend.service.AdminShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminShopServiceImpl implements AdminShopService {

    private final ShopItemRepository shopItemRepository;
    private final UserRepository userRepository;
    private final AdminAuditService adminAuditService;

    @Override
    @Transactional(readOnly = true)
    public List<AdminShopItemResponse> listAll() {
        return shopItemRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public AdminShopItemResponse create(UUID adminId, ShopItemRequest request) {
        ShopItem item = new ShopItem(
                request.name(), request.description(), parseType(request.type()),
                request.priceXp(), request.iconUrl(), request.exclusiveToAchievement());

        ShopItem saved = shopItemRepository.save(item);

        adminAuditService.log(adminOrThrow(adminId), AdminActionType.SHOP_ITEM_CREATED,
                "SHOP_ITEM", saved.getId(), "Item criado: " + saved.getName());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public AdminShopItemResponse update(UUID adminId, UUID itemId, ShopItemRequest request) {
        ShopItem item = findOrThrow(itemId);

        item.setName(request.name());
        item.setDescription(request.description());
        item.setType(parseType(request.type()));
        item.setPriceXp(request.priceXp());
        item.setIconUrl(request.iconUrl());
        item.setExclusiveToAchievement(request.exclusiveToAchievement());

        ShopItem saved = shopItemRepository.save(item);

        adminAuditService.log(adminOrThrow(adminId), AdminActionType.SHOP_ITEM_UPDATED,
                "SHOP_ITEM", saved.getId(), "Item atualizado: " + saved.getName());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deactivate(UUID adminId, UUID itemId) {
        ShopItem item = findOrThrow(itemId);
        item.setActive(false);
        shopItemRepository.save(item);

        adminAuditService.log(adminOrThrow(adminId), AdminActionType.SHOP_ITEM_DEACTIVATED,
                "SHOP_ITEM", itemId, "Item desativado: " + item.getName());
    }

    private ShopItem findOrThrow(UUID id) {
        return shopItemRepository.findById(id)
                .orElseThrow(() -> new ShopItemNotFoundException("Item nao encontrado"));
    }

    private ShopItemType parseType(String raw) {
        try {
            return ShopItemType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Tipo de item invalido: " + raw);
        }
    }

    private AdminShopItemResponse toResponse(ShopItem item) {
        return new AdminShopItemResponse(
                item.getId(), item.getName(), item.getDescription(), item.getType().name(),
                item.getPriceXp(), item.getIconUrl(), item.isActive(), item.isExclusiveToAchievement());
    }

    private com.mib.backend.entity.User adminOrThrow(UUID adminId) {
        return userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador nao encontrado"));
    }
}
