package com.mib.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MissionTemplateRequest(

        @NotBlank(message = "O titulo e obrigatorio")
        @Size(max = 120)
        String title,

        @NotBlank(message = "A descricao e obrigatoria")
        @Size(max = 300)
        String description,

        @NotBlank(message = "A categoria e obrigatoria")
        String category,

        @NotBlank(message = "A dificuldade e obrigatoria")
        String difficulty,

        @NotNull(message = "A recompensa em XP e obrigatoria")
        @Min(value = 1, message = "A recompensa deve ser de pelo menos 1 XP")
        Integer baseXpReward
) {
}
