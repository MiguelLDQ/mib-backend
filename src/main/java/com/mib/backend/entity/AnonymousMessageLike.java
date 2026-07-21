package com.mib.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "anonymous_message_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AnonymousMessageLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private AnonymousMessage message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public AnonymousMessageLike(AnonymousMessage message, User user) {
        this.message = message;
        this.user = user;
    }
}
