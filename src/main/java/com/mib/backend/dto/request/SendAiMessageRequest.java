package com.mib.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendAiMessageRequest(

        @NotBlank(message = "A mensagem nao pode estar vazia")
        @Size(max = 2000, message = "A mensagem deve ter no maximo 2000 caracteres")
        String content
) {
}
