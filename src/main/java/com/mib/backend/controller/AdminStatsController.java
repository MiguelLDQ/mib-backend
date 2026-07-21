package com.mib.backend.controller;

import com.mib.backend.dto.response.PlatformStatsResponse;
import com.mib.backend.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin - Stats", description = "Metricas e estatisticas gerais da plataforma")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping
    @Operation(summary = "Retorna estatisticas gerais da plataforma (usuarios, conteudo, moderacao)")
    public ResponseEntity<PlatformStatsResponse> getStats() {
        return ResponseEntity.ok(adminStatsService.getPlatformStats());
    }
}
