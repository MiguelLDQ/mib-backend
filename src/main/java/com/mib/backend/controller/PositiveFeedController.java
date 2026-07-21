package com.mib.backend.controller;

import com.mib.backend.dto.response.PositiveFeedItemResponse;
import com.mib.backend.service.PositiveFeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feed/positive")
@RequiredArgsConstructor
@Tag(name = "Positive Feed", description = "Frases, curiosidades e desafios rapidos, atualizados automaticamente todo dia")
public class PositiveFeedController {

    private final PositiveFeedService positiveFeedService;

    @GetMapping
    @Operation(summary = "Retorna o feed positivo do dia (o mesmo conjunto para todos, trocando automaticamente a cada dia)")
    public ResponseEntity<List<PositiveFeedItemResponse>> getTodayFeed(@RequestParam(defaultValue = "5") int count) {
        return ResponseEntity.ok(positiveFeedService.getTodayFeed(count));
    }
}
