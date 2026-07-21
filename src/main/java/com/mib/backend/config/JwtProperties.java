package com.mib.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mib.jwt")
public class JwtProperties {

    /** Segredo usado para assinar os tokens. Deve ter no minimo 256 bits em producao. */
    private String secret;

    private long accessTokenExpirationMs;

    private long refreshTokenExpirationMs;
}
