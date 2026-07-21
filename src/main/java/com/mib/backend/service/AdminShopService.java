package com.mib.backend.service;

import com.mib.backend.dto.request.ShopItemRequest;
import com.mib.backend.dto.response.AdminShopItemResponse;

import java.util.List;
import java.util.UUID;

public interface AdminShopService {

    List<AdminShopItemResponse> listAll();

    AdminShopItemResponse create(UUID adminId, ShopItemRequest request);

    AdminShopItemResponse update(UUID adminId, UUID itemId, ShopItemRequest request);

    void deactivate(UUID adminId, UUID itemId);
}
