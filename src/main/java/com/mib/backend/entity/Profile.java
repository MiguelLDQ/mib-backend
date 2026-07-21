package com.mib.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dados publicos de perfil do usuario (nome de exibicao, avatar, bio, status).
 * Personalizacoes cosmeticas equipadas (molduras, fundos, emblemas, titulos, temas)
 * serao referenciadas aqui no modulo de loja/inventario (fase futura).
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Profile extends BaseEntity {

    @OneToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "display_name", length = 80)
    private String displayName;

    @Column(length = 300)
    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "status_message", length = 120)
    private String statusMessage;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    public Profile(User user, String displayName) {
        this.user = user;
        this.displayName = displayName;
    }
}
