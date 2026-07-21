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
@Table(name = "moderation_logs")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ModerationLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private ContentTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModerationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModerationActionType action;

    @Column(length = 300)
    private String detail;

    public ModerationLog(User user, ContentTargetType targetType, UUID targetId,
                          ModerationCategory category, ModerationActionType action, String detail) {
        this.user = user;
        this.targetType = targetType;
        this.targetId = targetId;
        this.category = category;
        this.action = action;
        this.detail = detail;
    }
}
