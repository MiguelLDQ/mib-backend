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

/**
 * Trilha de auditoria de acoes tomadas manualmente por administradores (distinta do
 * {@link ModerationLog}, que registra apenas infracoes detectadas automaticamente).
 */
@Entity
@Table(name = "admin_logs")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AdminLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AdminActionType action;

    @Column(name = "target_type", length = 30)
    private String targetType;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(length = 300)
    private String detail;

    public AdminLog(User admin, AdminActionType action, String targetType, UUID targetId, String detail) {
        this.admin = admin;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.detail = detail;
    }
}
