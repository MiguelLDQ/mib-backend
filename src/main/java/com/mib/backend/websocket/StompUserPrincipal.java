package com.mib.backend.websocket;

import java.security.Principal;
import java.util.UUID;

public record StompUserPrincipal(UUID userId, String username) implements Principal {

    @Override
    public String getName() {
        // O nome do Principal vira o identificador usado pelo Spring para /user/**
        // destinations; usamos o userId para permitir roteamento direto por usuario.
        return userId.toString();
    }
}
