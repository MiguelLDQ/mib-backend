package com.mib.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReportRequest(

        @NotNull(message = "O tipo do alvo e obrigatorio")
        String targetType,

        @NotNull(message = "O id do alvo e obrigatorio")
        UUID targetId,

        @NotNull(message = "O motivo da denuncia e obrigatorio")
        String reason,

        @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres")
        String description
) {
}
