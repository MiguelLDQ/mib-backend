package com.mib.backend.service.impl;

import com.mib.backend.config.StorageProperties;
import com.mib.backend.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Armazena avatares no disco local do servidor. Gratuito e sem dependencias externas.
 * Nota de infraestrutura: em provedores com disco efemero (ex.: free tier do Render sem
 * volume persistente), os arquivos podem ser perdidos em um redeploy. Para producao,
 * recomenda-se anexar um disco persistente ou, futuramente, trocar esta implementacao
 * por um bucket compativel com S3 usando um provedor com camada gratuita.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/webp");

    private final StorageProperties storageProperties;

    public String store(UUID userId, MultipartFile file) {
        validate(file);

        try {
            Path uploadDir = Path.of(storageProperties.getAvatarDir()).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String extension = extensionFor(file.getContentType());
            String filename = userId + "-" + UUID.randomUUID() + extension;
            Path target = uploadDir.resolve(filename);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return storageProperties.getAvatarPublicPath() + "/" + filename;
        } catch (IOException ex) {
            log.error("Falha ao salvar avatar do usuario {}", userId, ex);
            throw new InvalidFileException("Nao foi possivel salvar a imagem enviada");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Nenhum arquivo foi enviado");
        }
        if (file.getSize() > storageProperties.getMaxAvatarSizeBytes()) {
            throw new InvalidFileException("A imagem deve ter no maximo 2MB");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidFileException("Formato de imagem invalido. Use PNG, JPEG ou WEBP");
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
