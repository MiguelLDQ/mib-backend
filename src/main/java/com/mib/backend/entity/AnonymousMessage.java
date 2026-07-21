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

/**
 * Mensagem anonima do mural "Estrelas Pontilhadas". O autor e mantido internamente
 * (necessario para moderacao, denuncia e historico de infracoes), mas nunca e exposto
 * nas respostas da API — a anonimidade e garantida na camada de DTO/mapper, nao no banco.
 */
@Entity
@Table(name = "anonymous_messages")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AnonymousMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    private AnonymousMessage parentMessage;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "reply_count", nullable = false)
    private int replyCount = 0;

    @Column(nullable = false)
    private boolean removedByModeration = false;

    @Column(name = "removed_by_author", nullable = false)
    private boolean removedByAuthor = false;

    public AnonymousMessage(User author, AnonymousMessage parentMessage, String content) {
        this.author = author;
        this.parentMessage = parentMessage;
        this.content = content;
    }

    public boolean isVisible() {
        return !removedByModeration && !removedByAuthor;
    }
}
