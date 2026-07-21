package com.mib.backend.controller;

import com.mib.backend.dto.response.InventoryItemResponse;
import com.mib.backend.dto.response.ShopItemResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
@Tag(name = "Shop", description = "Loja de personalizacao (itens cosmeticos comprados com XP)")
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/items")
    @Operation(summary = "Lista os itens disponiveis para compra, com indicacao dos ja possuidos")
    public ResponseEntity<List<ShopItemResponse>> listItems() {
        return ResponseEntity.ok(shopService.listAvailableItems(SecurityUtils.currentUserId()));
    }

    @PostMapping("/items/{shopItemId}/purchase")
    @Operation(summary = "Compra um item, debitando XP da carteira do usuario")
    public ResponseEntity<InventoryItemResponse> purchase(@PathVariable UUID shopItemId) {
        return ResponseEntity.status(201).body(shopService.purchase(SecurityUtils.currentUserId(), shopItemId));
    }
}
