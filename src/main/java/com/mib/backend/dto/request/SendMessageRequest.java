package com.mib.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(

        @NotBlank(message = "A mensagem nao pode estar vazia")
        @Size(max = 1000, message = "A mensagem deve ter no maximo 1000 caracteres")
        String content
) {
}
