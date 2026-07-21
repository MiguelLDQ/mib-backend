package com.mib.backend.entity;

/**
 * Niveis de permissao do sistema.
 * ROLE_USER: usuario comum.
 * ROLE_ADMIN: acesso total ao painel administrativo.
 * ROLE_MODERATOR: reservado para futura expansao (moderacao manual sem acesso total de admin).
 */
public enum RoleName {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_MODERATOR
}
