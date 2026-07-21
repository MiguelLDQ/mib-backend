package com.mib.backend.controller;

import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.dto.response.XpHistoryEntryResponse;
import com.mib.backend.dto.response.XpSummaryResponse;
import com.mib.backend.entity.User;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.repository.XpHistoryRepository;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.XpService;
import com.mib.backend.util.LevelCalculator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/xp")
@RequiredArgsConstructor
@Tag(name = "XP", description = "Progresso de experiencia e nivel do usuario")
public class XpController {

    private final XpService xpService;
    private final UserRepository userRepository;
    private final XpHistoryRepository xpHistoryRepository;

    @GetMapping("/me")
    @Operation(summary = "Retorna XP total, nivel atual e progresso para o proximo nivel")
    public ResponseEntity<XpSummaryResponse> getMySummary() {
        User user = currentUser();
        LevelCalculator.LevelProgress progress = xpService.getProgress(user);

        return ResponseEntity.ok(new XpSummaryResponse(
                progress.totalXp(), progress.level(), progress.currentLevelXp(),
                progress.xpForNextLevel(), progress.progressPercentage(), user.getXpWallet()));
    }

    @GetMapping("/me/history")
    @Operation(summary = "Historico paginado de ganhos de XP")
    public ResponseEntity<PagedResponse<XpHistoryEntryResponse>> getMyHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = xpHistoryRepository.findByUserIdOrderByCreatedAtDesc(SecurityUtils.currentUserId(), pageable)
                .map(h -> new XpHistoryEntryResponse(
                        h.getAmount(), h.getReason().name(), h.getDescription(), h.getLevelAfter(), h.getCreatedAt()));

        return ResponseEntity.ok(PagedResponse.from(result));
    }

    private User currentUser() {
        return userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
