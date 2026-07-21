package com.mib.backend.controller;

import com.mib.backend.dto.response.AchievementResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
@Tag(name = "Achievements", description = "Conquistas do usuario, desbloqueadas ou nao")
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    @Operation(summary = "Lista todas as conquistas do sistema com o status de desbloqueio do usuario autenticado")
    public ResponseEntity<List<AchievementResponse>> list() {
        return ResponseEntity.ok(achievementService.listForUser(SecurityUtils.currentUserId()));
    }
}
