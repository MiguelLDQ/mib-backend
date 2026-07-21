package com.mib.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Informe o email ou nome de usuario")
        String emailOrUsername,

        @NotBlank(message = "A senha e obrigatoria")
        String password
) {
}
