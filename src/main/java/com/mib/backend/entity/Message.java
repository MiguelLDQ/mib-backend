package com.mib.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Message extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 1000)
    private String content;

    /** Usado pelo modulo de moderacao automatica (Fase 5) para ocultar mensagens sem apagar o historico. */
    @Column(nullable = false)
    private boolean removedByModeration = false;

    public Message(ChatRoom chatRoom, User sender, String content) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.content = content;
    }
}
