package com.mib.backend.controller;

import com.mib.backend.dto.request.SendMessageRequest;
import com.mib.backend.dto.response.DirectRoomResponse;
import com.mib.backend.dto.response.MessageResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.ChatService;
import com.mib.backend.websocket.ChatBroadcastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints REST do chat: historico paginado, gestao de conversas privadas e um
 * fallback de envio via HTTP (o caminho principal de envio em tempo real e o
 * WebSocket, ver {@link com.mib.backend.websocket.ChatWebSocketController}).
 * Ambos os caminhos passam pelo mesmo {@link ChatService} e disparam o mesmo broadcast.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat geral e conversas privadas entre amigos")
public class ChatController {

    private final ChatService chatService;
    private final ChatBroadcastService chatBroadcastService;

    @GetMapping("/general/room")
    @Operation(summary = "Retorna o id da sala geral")
    public ResponseEntity<UUID> getGeneralRoomId() {
        return ResponseEntity.ok(chatService.getOrCreateGeneralRoomId());
    }

    @GetMapping("/direct")
    @Operation(summary = "Lista as conversas privadas existentes do usuario autenticado")
    public ResponseEntity<List<DirectRoomResponse>> listDirectRooms() {
        return ResponseEntity.ok(chatService.listDirectRooms(SecurityUtils.currentUserId()));
    }

    @GetMapping("/direct/{friendUserId}")
    @Operation(summary = "Retorna (ou cria) a conversa privada com um amigo")
    public ResponseEntity<DirectRoomResponse> getOrCreateDirectRoom(@PathVariable UUID friendUserId) {
        return ResponseEntity.ok(chatService.getOrCreateDirectRoom(SecurityUtils.currentUserId(), friendUserId));
    }

    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "Historico paginado de mensagens de uma sala (geral ou privada)")
    public ResponseEntity<PagedResponse<MessageResponse>> getHistory(
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {

        return ResponseEntity.ok(chatService.getRoomHistory(SecurityUtils.currentUserId(), roomId, page, size));
    }

    @PostMapping("/rooms/{roomId}/messages")
    @Operation(summary = "Envia uma mensagem via HTTP (fallback do envio em tempo real por WebSocket)")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable UUID roomId,
                                                         @Valid @RequestBody SendMessageRequest request) {
        MessageResponse message = chatService.sendMessage(SecurityUtils.currentUserId(), roomId, request.content());
        chatBroadcastService.broadcast(message);
        return ResponseEntity.status(201).body(message);
    }
}
