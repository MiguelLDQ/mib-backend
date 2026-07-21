package com.mib.backend.controller;

import com.mib.backend.dto.request.MissionTemplateRequest;
import com.mib.backend.dto.response.MissionTemplateResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.AdminMissionService;
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
@RequestMapping("/api/admin/missions/templates")
@RequiredArgsConstructor
@Tag(name = "Admin - Missions", description = "CRUD de templates de missao diaria")
public class AdminMissionController {

    private final AdminMissionService adminMissionService;

    @GetMapping
    @Operation(summary = "Lista todos os templates de missao (ativos e inativos)")
    public ResponseEntity<List<MissionTemplateResponse>> listAll() {
        return ResponseEntity.ok(adminMissionService.listAll());
    }

    @PostMapping
    @Operation(summary = "Cria um novo template de missao")
    public ResponseEntity<MissionTemplateResponse> create(@Valid @RequestBody MissionTemplateRequest request) {
        var created = adminMissionService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{templateId}")
    @Operation(summary = "Atualiza um template de missao")
    public ResponseEntity<MissionTemplateResponse> update(@PathVariable UUID templateId,
                                                            @Valid @RequestBody MissionTemplateRequest request) {
        return ResponseEntity.ok(adminMissionService.update(SecurityUtils.currentUserId(), templateId, request));
    }

    @DeleteMapping("/{templateId}")
    @Operation(summary = "Desativa um template (nao sera mais sorteado; historico e preservado)")
    public ResponseEntity<Void> deactivate(@PathVariable UUID templateId) {
        adminMissionService.deactivate(SecurityUtils.currentUserId(), templateId);
        return ResponseEntity.noContent().build();
    }
}
