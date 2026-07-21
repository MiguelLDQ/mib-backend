package com.mib.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Usuario do sistema MIB.
 * Contem apenas dados de conta/autenticacao. Dados de exibicao (nome, avatar, bio,
 * status) ficam em {@link Profile}, e dados de progressao (XP, nivel) serao
 * adicionados no modulo XP (fase seguinte), mantendo responsabilidades separadas.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    // --- Campos de moderacao (usados pelo modulo de moderacao automatica) ---
    @Column(name = "warning_count", nullable = false)
    private int warningCount = 0;

    @Column(name = "suspended_until")
    private Instant suspendedUntil;

    @Column(name = "banned", nullable = false)
    private boolean banned = false;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    @Column(name = "total_xp", nullable = false)
    private long totalXp = 0;

    @Column(name = "level", nullable = false)
    private int level = 1;

    /** Saldo de XP disponivel para gastar na loja. Separado de totalXp (que e o
     * historico vitalicio usado para calcular o nivel e nunca diminui). */
    @Column(name = "xp_wallet", nullable = false)
    private long xpWallet = 0;

    @Column(name = "current_login_streak", nullable = false)
    private int currentLoginStreak = 0;

    @Column(name = "longest_login_streak", nullable = false)
    private int longestLoginStreak = 0;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Profile profile;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public boolean isCurrentlySuspended() {
        return suspendedUntil != null && suspendedUntil.isAfter(Instant.now());
    }
}
