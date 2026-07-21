package com.mib.backend.controller;

import com.mib.backend.dto.response.InventoryItemResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.InventoryService;
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
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Itens possuidos pelo usuario e controle de equipar/desequipar")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "Lista os itens do inventario do usuario, com estado de equipado")
    public ResponseEntity<List<InventoryItemResponse>> listInventory() {
        return ResponseEntity.ok(inventoryService.listOwnInventory(SecurityUtils.currentUserId()));
    }

    @PostMapping("/{shopItemId}/equip")
    @Operation(summary = "Equipa um item possuido (desequipa automaticamente outro do mesmo tipo, exceto emblemas)")
    public ResponseEntity<InventoryItemResponse> equip(@PathVariable UUID shopItemId) {
        return ResponseEntity.ok(inventoryService.equip(SecurityUtils.currentUserId(), shopItemId));
    }

    @PostMapping("/{shopItemId}/unequip")
    @Operation(summary = "Desequipa um item")
    public ResponseEntity<InventoryItemResponse> unequip(@PathVariable UUID shopItemId) {
        return ResponseEntity.ok(inventoryService.unequip(SecurityUtils.currentUserId(), shopItemId));
    }
}
