package com.mib.backend.websocket;

import com.mib.backend.exception.BadRequestException;
import com.mib.backend.service.ChatService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * Endpoints STOMP para envio de mensagens em tempo real. O cliente conecta em /ws,
 * envia para /app/chat.general ou /app/chat.room/{roomId}, e recebe mensagens
 * assinando /topic/chat.general (sala geral) ou /topic/chat.room.{roomId} (privada).
 * A autenticacao e feita no handshake pelo {@link WebSocketAuthInterceptor}.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final ChatBroadcastService chatBroadcastService;

    @MessageMapping("/chat.general")
    public void sendToGeneralRoom(@Payload OutgoingChatMessage payload, Principal principal) {
        validate(payload);
        UUID senderId = extractUserId(principal);
        UUID roomId = chatService.getOrCreateGeneralRoomId();

        var message = chatService.sendMessage(senderId, roomId, payload.content());
        chatBroadcastService.broadcast(message);
    }

    @MessageMapping("/chat.room/{roomId}")
    public void sendToRoom(@DestinationVariable UUID roomId, @Payload OutgoingChatMessage payload, Principal principal) {
        validate(payload);
        UUID senderId = extractUserId(principal);

        var message = chatService.sendMessage(senderId, roomId, payload.content());
        chatBroadcastService.broadcast(message);
    }

    private void validate(OutgoingChatMessage payload) {
        if (payload == null || payload.content() == null || payload.content().isBlank()) {
            throw new BadRequestException("A mensagem nao pode estar vazia");
        }
        if (payload.content().length() > 1000) {
            throw new BadRequestException("A mensagem deve ter no maximo 1000 caracteres");
        }
    }

    /**
     * Canal de erro pessoal: se o processamento de uma mensagem falhar (ex.: sala
     * invalida, sem permissao), o cliente pode assinar /user/queue/errors para
     * exibir feedback, ao inves de a conexao simplesmente cair em silencio.
     */
    @org.springframework.messaging.handler.annotation.MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Exception ex) {
        log.debug("Erro ao processar mensagem STOMP: {}", ex.getMessage());
        return ex.getMessage() != null ? ex.getMessage() : "Nao foi possivel processar a mensagem";
    }

    private UUID extractUserId(Principal principal) {
        if (principal instanceof StompUserPrincipal stompUserPrincipal) {
            return stompUserPrincipal.userId();
        }
        throw new IllegalStateException("Usuario nao autenticado na sessao WebSocket");
    }

    public record OutgoingChatMessage(
            @NotBlank(message = "A mensagem nao pode estar vazia")
            @Size(max = 1000, message = "A mensagem deve ter no maximo 1000 caracteres")
            String content
    ) {
    }
}
