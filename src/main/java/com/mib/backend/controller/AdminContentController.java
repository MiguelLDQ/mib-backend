package com.mib.backend.controller;

import com.mib.backend.dto.request.ReviewReportRequest;
import com.mib.backend.dto.response.ModerationLogResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.dto.response.ReportResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.AdminContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Content", description = "Moderacao manual de mensagens, denuncias e logs")
public class AdminContentController {

    private final AdminContentService adminContentService;

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "Remove uma mensagem do chat (geral ou privado)")
    public ResponseEntity<Void> deleteChatMessage(@PathVariable UUID messageId) {
        adminContentService.deleteChatMessage(SecurityUtils.currentUserId(), messageId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/anonymous-messages/{messageId}")
    @Operation(summary = "Remove uma mensagem do mural Estrelas Pontilhadas")
    public ResponseEntity<Void> deleteAnonymousMessage(@PathVariable UUID messageId) {
        adminContentService.deleteAnonymousMessage(SecurityUtils.currentUserId(), messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports")
    @Operation(summary = "Lista denuncias por status (padrao: PENDING)")
    public ResponseEntity<PagedResponse<ReportResponse>> listReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminContentService.listReports(status, page, size));
    }

    @PostMapping("/reports/{reportId}/review")
    @Operation(summary = "Marca uma denuncia como REVIEWED ou DISMISSED")
    public ResponseEntity<ReportResponse> reviewReport(@PathVariable UUID reportId,
                                                         @Valid @RequestBody ReviewReportRequest request) {
        return ResponseEntity.ok(adminContentService.reviewReport(
                SecurityUtils.currentUserId(), reportId, request.status()));
    }

    @GetMapping("/moderation-logs")
    @Operation(summary = "Lista o historico de acoes tomadas pela moderacao automatica")
    public ResponseEntity<PagedResponse<ModerationLogResponse>> listModerationLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminContentService.listModerationLogs(page, size));
    }
}
