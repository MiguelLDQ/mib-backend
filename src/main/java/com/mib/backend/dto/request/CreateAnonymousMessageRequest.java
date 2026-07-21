package com.mib.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAnonymousMessageRequest(

        @NotBlank(message = "A mensagem nao pode estar vazia")
        @Size(max = 500, message = "A mensagem deve ter no maximo 500 caracteres")
        String content,

        /** Nulo para uma publicacao nova; preenchido para responder a uma mensagem existente. */
        UUID parentMessageId
) {
}
