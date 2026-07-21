package com.mib.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecordMoodRequest(

        @NotBlank(message = "O nivel de humor e obrigatorio")
        String moodLevel,

        @Size(max = 300, message = "A nota deve ter no maximo 300 caracteres")
        String note
) {
}
