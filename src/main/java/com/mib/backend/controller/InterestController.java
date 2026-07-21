package com.mib.backend.controller;

import com.mib.backend.dto.request.UpdateInterestsRequest;
import com.mib.backend.dto.response.InterestResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.InterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
@Tag(name = "Interests", description = "Catalogo de interesses e selecao do usuario (alimenta recomendacao e salas tematicas)")
public class InterestController {

    private final InterestService interestService;

    @GetMapping
    @Operation(summary = "Lista o catalogo de interesses, indicando quais o usuario ja selecionou")
    public ResponseEntity<List<InterestResponse>> getCatalog() {
        return ResponseEntity.ok(interestService.getCatalog(SecurityUtils.currentUserId()));
    }

    @PutMapping("/me")
    @Operation(summary = "Substitui a lista de interesses do usuario (entra/sai das salas tematicas automaticamente)")
    public ResponseEntity<List<InterestResponse>> updateMyInterests(@Valid @RequestBody UpdateInterestsRequest request) {
        return ResponseEntity.ok(interestService.updateMyInterests(SecurityUtils.currentUserId(), request));
    }
}
