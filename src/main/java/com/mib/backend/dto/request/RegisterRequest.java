package com.mib.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "O nome de usuario e obrigatorio")
        @Size(min = 3, max = 30, message = "O nome de usuario deve ter entre 3 e 30 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "O nome de usuario deve conter apenas letras, numeros e underscore")
        String username,

        @NotBlank(message = "O email e obrigatorio")
        @Email(message = "Email invalido")
        String email,

        @NotBlank(message = "A senha e obrigatoria")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "A senha deve conter ao menos uma letra maiuscula, uma minuscula e um numero"
        )
        String password,

        @NotBlank(message = "O nome de exibicao e obrigatorio")
        @Size(max = 80)
        String displayName
) {
}
