package com.mib.backend.controller;

import com.mib.backend.dto.request.SuspendUserRequest;
import com.mib.backend.dto.response.AdminUserResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Endpoints exclusivos de administradores (ROLE_ADMIN), ja protegidos globalmente por
 * {@code SecurityConfig} (todo o prefixo /api/admin/** exige a role).
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Users", description = "Gestao de usuarios pelo painel administrativo")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "Busca/lista usuarios (username, email ou nome de exibicao)")
    public ResponseEntity<PagedResponse<AdminUserResponse>> search(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminUserService.search(query, page, size));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Detalha um usuario")
    public ResponseEntity<AdminUserResponse> getById(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminUserService.getById(userId));
    }

    @PostMapping("/{userId}/suspend")
    @Operation(summary = "Suspende temporariamente uma conta")
    public ResponseEntity<AdminUserResponse> suspend(@PathVariable UUID userId,
                                                       @Valid @RequestBody SuspendUserRequest request) {
        return ResponseEntity.ok(adminUserService.suspend(
                SecurityUtils.currentUserId(), userId, request.hours(), request.reason()));
    }

    @PostMapping("/{userId}/unsuspend")
    @Operation(summary = "Remove a suspensao de uma conta")
    public ResponseEntity<AdminUserResponse> unsuspend(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminUserService.unsuspend(SecurityUtils.currentUserId(), userId));
    }

    @PostMapping("/{userId}/ban")
    @Operation(summary = "Bane uma conta permanentemente")
    public ResponseEntity<AdminUserResponse> ban(@PathVariable UUID userId,
                                                   @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(adminUserService.ban(SecurityUtils.currentUserId(), userId, reason));
    }

    @PostMapping("/{userId}/unban")
    @Operation(summary = "Remove o banimento de uma conta")
    public ResponseEntity<AdminUserResponse> unban(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminUserService.unban(SecurityUtils.currentUserId(), userId));
    }

    @PostMapping("/{userId}/roles/admin/grant")
    @Operation(summary = "Concede a role ROLE_ADMIN a um usuario")
    public ResponseEntity<AdminUserResponse> grantAdmin(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminUserService.grantAdminRole(SecurityUtils.currentUserId(), userId));
    }

    @PostMapping("/{userId}/roles/admin/revoke")
    @Operation(summary = "Remove a role ROLE_ADMIN de um usuario")
    public ResponseEntity<AdminUserResponse> revokeAdmin(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminUserService.revokeAdminRole(SecurityUtils.currentUserId(), userId));
    }
}
