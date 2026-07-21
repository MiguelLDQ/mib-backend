package com.mib.backend.controller;

import com.mib.backend.dto.request.RecordMoodRequest;
import com.mib.backend.dto.response.MoodResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.MoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mood")
@RequiredArgsConstructor
@Tag(name = "Mood", description = "Diario de humor pessoal (autorrelato, nunca usado para moderacao)")
public class MoodController {

    private final MoodService moodService;

    @PostMapping
    @Operation(summary = "Registra (ou atualiza) o humor de hoje")
    public ResponseEntity<MoodResponse> recordToday(@Valid @RequestBody RecordMoodRequest request) {
        return ResponseEntity.ok(moodService.recordToday(SecurityUtils.currentUserId(), request));
    }

    @GetMapping("/today")
    @Operation(summary = "Retorna o registro de humor de hoje, se ja existir")
    public ResponseEntity<MoodResponse> getToday() {
        return moodService.getToday(SecurityUtils.currentUserId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/history")
    @Operation(summary = "Historico de humor dos ultimos N dias (padrao 30), para montar o grafico pessoal")
    public ResponseEntity<List<MoodResponse>> getHistory(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(moodService.getHistory(SecurityUtils.currentUserId(), days));
    }
}
