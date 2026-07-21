package com.mib.backend.controller;

import com.mib.backend.dto.request.CompleteBreathingSessionRequest;
import com.mib.backend.dto.response.BreathingStatsResponse;
import com.mib.backend.dto.response.BreathingTechniqueResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.BreathingService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/breathing")
@RequiredArgsConstructor
@Tag(name = "Breathing", description = "Tecnicas guiadas de respiracao e estatisticas de uso")
public class BreathingController {

    private final BreathingService breathingService;

    @GetMapping("/techniques")
    @Operation(summary = "Lista as tecnicas de respiracao disponiveis, com padrao de tempo para animacao")
    public ResponseEntity<List<BreathingTechniqueResponse>> listTechniques() {
        return ResponseEntity.ok(breathingService.listTechniques());
    }

    @PostMapping("/techniques/{techniqueId}/complete")
    @Operation(summary = "Registra uma sessao concluida (credita XP ate 5 vezes por dia) e desbloqueia a conquista de primeira sessao")
    public ResponseEntity<Void> completeSession(@PathVariable UUID techniqueId,
                                                 @Valid @RequestBody CompleteBreathingSessionRequest request) {
        breathingService.completeSession(SecurityUtils.currentUserId(), techniqueId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Estatisticas de uso do modulo de respiracao pelo usuario autenticado")
    public ResponseEntity<BreathingStatsResponse> getStats() {
        return ResponseEntity.ok(breathingService.getStats(SecurityUtils.currentUserId()));
    }
}
