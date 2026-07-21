package com.mib.backend.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(

        @NotBlank(message = "O titulo e obrigatorio")
        @Size(max = 120, message = "O titulo deve ter no maximo 120 caracteres")
        String title,

        @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres")
        String description,

        @Size(max = 50, message = "A categoria deve ter no maximo 50 caracteres")
        String category,

        @FutureOrPresent(message = "A data de vencimento nao pode estar no passado")
        LocalDate dueDate
) {
}
