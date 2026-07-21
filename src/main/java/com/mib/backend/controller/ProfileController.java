package com.mib.backend.controller;

import com.mib.backend.dto.request.UpdateProfileRequest;
import com.mib.backend.dto.response.ProfileResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Visualizacao e edicao de perfil")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    @Operation(summary = "Retorna o perfil do usuario autenticado")
    public ResponseEntity<ProfileResponse> getOwnProfile() {
        return ResponseEntity.ok(profileService.getOwnProfile(SecurityUtils.currentUserId()));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Retorna o perfil publico de outro usuario (ou privado, se forem amigos)")
    public ResponseEntity<ProfileResponse> getPublicProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(profileService.getPublicProfile(userId, SecurityUtils.currentUserId()));
    }

    @PutMapping("/me")
    @Operation(summary = "Atualiza nome de exibicao, bio, status e visibilidade do perfil")
    public ResponseEntity<ProfileResponse> updateOwnProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateOwnProfile(SecurityUtils.currentUserId(), request));
    }

    @PostMapping("/me/avatar")
    @Operation(summary = "Envia/atualiza a foto de perfil (PNG, JPEG ou WEBP, ate 2MB)")
    public ResponseEntity<ProfileResponse> updateAvatar(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.updateAvatar(SecurityUtils.currentUserId(), file));
    }
}
