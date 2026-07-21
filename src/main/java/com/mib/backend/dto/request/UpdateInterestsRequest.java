package com.mib.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateInterestsRequest(

        @NotNull(message = "A lista de interesses e obrigatoria (pode ser vazia)")
        @Size(max = 30, message = "Selecione no maximo 30 interesses")
        List<UUID> interestIds
) {
}
