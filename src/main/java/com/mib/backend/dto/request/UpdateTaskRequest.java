package com.mib.backend.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(

        @Size(max = 120, message = "O titulo deve ter no maximo 120 caracteres")
        String title,

        @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres")
        String description,

        @Size(max = 50, message = "A categoria deve ter no maximo 50 caracteres")
        String category,

        LocalDate dueDate,

        /** PENDING, IN_PROGRESS ou COMPLETED. Use os endpoints dedicados de conclusao/reabertura
         * quando o objetivo for apenas mudar o status — este campo existe para edicoes completas. */
        String status
) {
}
