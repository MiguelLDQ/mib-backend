package com.mib.backend.controller;

import com.mib.backend.dto.response.DailyMissionResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.MissionService;
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
@RequestMapping("/api/missions/daily")
@RequiredArgsConstructor
@Tag(name = "Daily Missions", description = "Missoes diarias geradas automaticamente")
public class MissionController {

    private final MissionService missionService;

    @GetMapping
    @Operation(summary = "Lista as missoes do dia com status de conclusao do usuario autenticado")
    public ResponseEntity<List<DailyMissionResponse>> getTodayMissions() {
        return ResponseEntity.ok(missionService.getTodayMissions(SecurityUtils.currentUserId()));
    }

    @PostMapping("/{dailyMissionId}/complete")
    @Operation(summary = "Marca uma missao do dia como concluida e concede XP")
    public ResponseEntity<Void> completeMission(@PathVariable UUID dailyMissionId) {
        missionService.completeMission(SecurityUtils.currentUserId(), dailyMissionId);
        return ResponseEntity.noContent().build();
    }
}
