package com.mib.backend.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @Size(max = 80, message = "O nome de exibicao deve ter no maximo 80 caracteres")
        String displayName,

        @Size(max = 300, message = "A bio deve ter no maximo 300 caracteres")
        String bio,

        @Size(max = 120, message = "O status deve ter no maximo 120 caracteres")
        String statusMessage,

        Boolean isPublic
) {
}
