package com.mib.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CompleteBreathingSessionRequest(

        @NotNull(message = "A duracao da sessao e obrigatoria")
        @Min(value = 1, message = "A duracao deve ser de pelo menos 1 segundo")
        Integer durationSeconds
) {
}
