package com.mib.backend.service;

import com.mib.backend.dto.response.DirectRoomResponse;
import com.mib.backend.dto.response.MessageResponse;
import com.mib.backend.dto.response.PagedResponse;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    /** Retorna o id da sala geral, criando-a se ainda nao existir. */
    UUID getOrCreateGeneralRoomId();

    /** Retorna (ou cria) a sala privada entre o usuario atual e um amigo. Exige amizade aceita. */
    DirectRoomResponse getOrCreateDirectRoom(UUID currentUserId, UUID friendUserId);

    /** Lista as conversas privadas existentes do usuario, com dados do outro participante. */
    List<DirectRoomResponse> listDirectRooms(UUID currentUserId);

    PagedResponse<MessageResponse> getRoomHistory(UUID currentUserId, UUID roomId, int page, int size);

    /** Envia uma mensagem, persiste e retorna o DTO pronto para broadcast via WebSocket. */
    MessageResponse sendMessage(UUID senderId, UUID roomId, String content);

    /** Valida se o usuario pode ler/escrever na sala (geral: qualquer autenticado; privada: so participantes). */
    void assertCanAccessRoom(UUID userId, UUID roomId);
}
