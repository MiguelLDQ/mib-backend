package com.mib.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ShopItemRequest(

        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 100)
        String name,

        @Size(max = 300)
        String description,

        @NotBlank(message = "O tipo e obrigatorio")
        String type,

        @NotNull(message = "O preco em XP e obrigatorio")
        @Min(value = 0, message = "O preco nao pode ser negativo")
        Integer priceXp,

        String iconUrl,

        boolean exclusiveToAchievement
) {
}
