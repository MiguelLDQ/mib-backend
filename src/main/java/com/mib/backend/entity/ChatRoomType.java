package com.mib.backend.entity;

public enum ChatRoomType {
    /** Sala geral, unica, aberta a todos os usuarios autenticados. */
    GENERAL,
    /** Conversa privada entre dois amigos. */
    DIRECT,
    /** Reservado para a Fase 8 (salas tematicas por interesse). */
    THEME
}
