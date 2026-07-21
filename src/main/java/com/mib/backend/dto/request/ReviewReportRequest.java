package com.mib.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReviewReportRequest(

        @NotBlank(message = "O status e obrigatorio (REVIEWED ou DISMISSED)")
        String status
) {
}
