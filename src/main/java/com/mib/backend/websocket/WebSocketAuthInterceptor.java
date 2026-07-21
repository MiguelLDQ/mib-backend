package com.mib.backend.websocket;

import com.mib.backend.repository.ChatRoomParticipantRepository;
import com.mib.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Autentica a conexao STOMP (CONNECT) usando o header "Authorization: Bearer {token}"
 * enviado pelo cliente, e autoriza subscricoes (SUBSCRIBE) em salas privadas,
 * garantindo que apenas os participantes possam se inscrever no topico de uma
 * conversa direta. A sala geral (/topic/chat.room.{id da sala geral}) fica aberta a
 * qualquer usuario autenticado.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String DIRECT_ROOM_TOPIC_PREFIX = "/topic/chat.room.";

    private final JwtService jwtService;
    private final ChatRoomParticipantRepository participantRepository;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authorizeSubscription(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authHeader = firstNativeHeader(accessor, "Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Header Authorization ausente na conexao WebSocket");
        }

        String token = authHeader.substring("Bearer ".length());
        if (!jwtService.isTokenValid(token)) {
            throw new IllegalArgumentException("Token invalido ou expirado");
        }

        UUID userId = jwtService.extractUserId(token);
        accessor.setUser(new StompUserPrincipal(userId, userId.toString()));
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(DIRECT_ROOM_TOPIC_PREFIX)) {
            return; // sala geral ou outro destino publico: sem restricao adicional
        }

        if (!(accessor.getUser() instanceof StompUserPrincipal principal)) {
            throw new IllegalArgumentException("Usuario nao autenticado");
        }

        UUID roomId = UUID.fromString(destination.substring(DIRECT_ROOM_TOPIC_PREFIX.length()));
        boolean isParticipant = participantRepository.existsByChatRoomIdAndUserId(roomId, principal.userId());

        if (!isParticipant) {
            log.warn("Usuario {} tentou se inscrever em sala privada {} sem ser participante", principal.userId(), roomId);
            throw new IllegalArgumentException("Voce nao tem acesso a esta conversa");
        }
    }

    private String firstNativeHeader(StompHeaderAccessor accessor, String name) {
        var values = accessor.getNativeHeader(name);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
}
