package com.mib.backend.controller;

import com.mib.backend.dto.request.CreateReportRequest;
import com.mib.backend.dto.response.ReportResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Denuncias de mensagens ou perfis")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "Denuncia uma mensagem (chat ou anonima) ou um perfil. "
            + "targetType: CHAT_MESSAGE, ANONYMOUS_MESSAGE ou USER_PROFILE. "
            + "reason: BULLYING, SPAM, HARASSMENT, OFFENSIVE_CONTENT, FAKE_NEWS ou OTHER")
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody CreateReportRequest request) {
        var response = reportService.createReport(
                SecurityUtils.currentUserId(), request.targetType(), request.targetId(),
                request.reason(), request.description());
        return ResponseEntity.status(201).body(response);
    }
}
