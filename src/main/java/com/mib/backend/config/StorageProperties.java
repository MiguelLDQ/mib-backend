package com.mib.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mib.storage")
public class StorageProperties {

    /** Diretorio local onde os arquivos enviados sao gravados. */
    private String avatarDir = "uploads/avatars";

    /** Prefixo publico usado para servir os arquivos estaticos. */
    private String avatarPublicPath = "/uploads/avatars";

    /** Tamanho maximo de avatar aceito, em bytes (padrao 2MB). */
    private long maxAvatarSizeBytes = 2 * 1024 * 1024;
}
