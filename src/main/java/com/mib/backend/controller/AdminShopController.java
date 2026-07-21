package com.mib.backend.controller;

import com.mib.backend.dto.request.ShopItemRequest;
import com.mib.backend.dto.response.AdminShopItemResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.AdminShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/shop/items")
@RequiredArgsConstructor
@Tag(name = "Admin - Shop", description = "CRUD de itens da loja de personalizacao")
public class AdminShopController {

    private final AdminShopService adminShopService;

    @GetMapping
    @Operation(summary = "Lista todos os itens da loja (ativos e inativos)")
    public ResponseEntity<List<AdminShopItemResponse>> listAll() {
        return ResponseEntity.ok(adminShopService.listAll());
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo item na loja")
    public ResponseEntity<AdminShopItemResponse> create(@Valid @RequestBody ShopItemRequest request) {
        var created = adminShopService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "Atualiza um item da loja")
    public ResponseEntity<AdminShopItemResponse> update(@PathVariable UUID itemId,
                                                          @Valid @RequestBody ShopItemRequest request) {
        return ResponseEntity.ok(adminShopService.update(SecurityUtils.currentUserId(), itemId, request));
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Desativa um item (some da loja; quem ja possui continua com ele)")
    public ResponseEntity<Void> deactivate(@PathVariable UUID itemId) {
        adminShopService.deactivate(SecurityUtils.currentUserId(), itemId);
        return ResponseEntity.noContent().build();
    }
}
