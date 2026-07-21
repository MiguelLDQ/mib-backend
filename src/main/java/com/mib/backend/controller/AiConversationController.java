package com.mib.backend.controller;

import com.mib.backend.dto.request.SendAiMessageRequest;
import com.mib.backend.dto.response.AiMessageResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.AiConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * IA de apoio emocional. Nao substitui acompanhamento profissional — o proprio
 * prompt de sistema ({@link com.mib.backend.ai.AiSystemPrompt}) reforca isso na
 * conversa, e a mensagem de boas-vindas deixa isso claro no primeiro uso.
 */
@RestController
@RequestMapping("/api/ai/conversation")
@RequiredArgsConstructor
@Tag(name = "AI Support", description = "Conversa com a IA de apoio emocional (Groq/Llama)")
public class AiConversationController {

    private final AiConversationService aiConversationService;

    @GetMapping
    @Operation(summary = "Historico paginado da conversa do usuario com a IA")
    public ResponseEntity<PagedResponse<AiMessageResponse>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(aiConversationService.getHistory(SecurityUtils.currentUserId(), page, size));
    }

    @PostMapping("/messages")
    @Operation(summary = "Envia uma mensagem a IA e recebe a resposta (limitado por hora)")
    public ResponseEntity<AiMessageResponse> sendMessage(@Valid @RequestBody SendAiMessageRequest request) {
        return ResponseEntity.ok(aiConversationService.sendMessage(SecurityUtils.currentUserId(), request.content()));
    }

    @DeleteMapping
    @Operation(summary = "Apaga todo o historico de conversa do usuario com a IA")
    public ResponseEntity<Void> clearHistory() {
        aiConversationService.clearHistory(SecurityUtils.currentUserId());
        return ResponseEntity.noContent().build();
    }
}
