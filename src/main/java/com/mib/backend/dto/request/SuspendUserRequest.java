package com.mib.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SuspendUserRequest(

        @NotNull(message = "A duracao da suspensao e obrigatoria")
        @Min(value = 1, message = "A suspensao deve durar pelo menos 1 hora")
        Integer hours,

        String reason
) {
}
