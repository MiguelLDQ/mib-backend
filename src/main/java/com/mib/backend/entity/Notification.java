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

import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 300)
    private String message;

    @Column(name = "related_type", length = 30)
    private String relatedType;

    @Column(name = "related_id")
    private UUID relatedId;

    @Column(nullable = false)
    private boolean read = false;

    public Notification(User user, NotificationType type, String title, String message,
                         String relatedType, UUID relatedId) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedType = relatedType;
        this.relatedId = relatedId;
    }
}
