package com.mib.backend.controller;

import com.mib.backend.dto.request.CreateAnonymousMessageRequest;
import com.mib.backend.dto.response.AnonymousMessageResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.AnonymousMessageService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/anonymous")
@RequiredArgsConstructor
@Tag(name = "Anonymous Messages", description = "Mural Estrelas Pontilhadas: mensagens anonimas, respostas e curtidas")
public class AnonymousMessageController {

    private final AnonymousMessageService anonymousMessageService;

    @GetMapping
    @Operation(summary = "Feed paginado de mensagens anonimas (mais recentes primeiro)")
    public ResponseEntity<PagedResponse<AnonymousMessageResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(anonymousMessageService.getFeed(SecurityUtils.currentUserId(), page, size));
    }

    @GetMapping("/{messageId}/replies")
    @Operation(summary = "Lista as respostas de uma mensagem anonima")
    public ResponseEntity<List<AnonymousMessageResponse>> getReplies(@PathVariable UUID messageId) {
        return ResponseEntity.ok(anonymousMessageService.getReplies(SecurityUtils.currentUserId(), messageId));
    }

    @PostMapping
    @Operation(summary = "Publica uma mensagem anonima nova ou uma resposta")
    public ResponseEntity<AnonymousMessageResponse> create(@Valid @RequestBody CreateAnonymousMessageRequest request) {
        var response = anonymousMessageService.create(
                SecurityUtils.currentUserId(), request.content(), request.parentMessageId());
        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "Remove uma mensagem publicada pelo proprio usuario")
    public ResponseEntity<Void> deleteOwn(@PathVariable UUID messageId) {
        anonymousMessageService.deleteOwnMessage(SecurityUtils.currentUserId(), messageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{messageId}/like")
    @Operation(summary = "Alterna curtida (curte se nao tinha curtido, remove a curtida caso contrario)")
    public ResponseEntity<Map<String, Boolean>> toggleLike(@PathVariable UUID messageId) {
        boolean liked = anonymousMessageService.toggleLike(SecurityUtils.currentUserId(), messageId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }
}
