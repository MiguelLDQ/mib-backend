package com.mib.backend.controller;

import com.mib.backend.dto.response.FriendSuggestionResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.RecommendationService;
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
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Sugestoes de amizade com base em interesses em comum")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/friends")
    @Operation(summary = "Sugere pessoas com interesses em comum, ordenadas por compatibilidade")
    public ResponseEntity<List<FriendSuggestionResponse>> suggestFriends(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.suggestFriends(SecurityUtils.currentUserId(), limit));
    }
}
