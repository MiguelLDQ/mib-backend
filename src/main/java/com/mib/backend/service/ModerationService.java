package com.mib.backend.service;

import com.mib.backend.entity.ContentTargetType;
import com.mib.backend.entity.User;

import java.util.UUID;

public interface ModerationService {

    /**
     * Avalia o conteudo. Se houver infracao, registra o log, aplica a consequencia
     * (advertencia/suspensao/banimento) ao autor e retorna true (conteudo deve ser
     * ocultado/removido pelo chamador). Se nao houver infracao, retorna false.
     */
    boolean moderate(User author, ContentTargetType targetType, UUID targetId, String content);
}
