package com.mib.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Relacao de amizade entre dois usuarios.
 * requester = quem enviou o pedido. addressee = quem recebeu.
 * Enquanto status = PENDING, representa uma solicitacao aguardando resposta.
 * DECLINED e mantido (nao apagado) para evitar spam de novas solicitacoes repetidas
 * em curto intervalo; a regra de reenvio fica no service.
 */
@Entity
@Table(name = "friendships")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Friendship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(name = "responded_at")
    private Instant respondedAt;

    public Friendship(User requester, User addressee) {
        this.requester = requester;
        this.addressee = addressee;
        this.status = FriendshipStatus.PENDING;
    }

    public boolean involves(java.util.UUID userId) {
        return requester.getId().equals(userId) || addressee.getId().equals(userId);
    }
}
