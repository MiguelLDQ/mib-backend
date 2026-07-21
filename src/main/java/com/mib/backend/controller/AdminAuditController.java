package com.mib.backend.controller;

import com.mib.backend.dto.response.AdminLogResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.service.AdminAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@Tag(name = "Admin - Audit Logs", description = "Trilha de auditoria de acoes administrativas manuais")
public class AdminAuditController {

    private final AdminAuditService adminAuditService;

    @GetMapping
    @Operation(summary = "Lista o historico de acoes administrativas (suspensoes, banimentos, edicoes, etc.)")
    public ResponseEntity<PagedResponse<AdminLogResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminAuditService.list(page, size));
    }
}
