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
@Table(name = "breathing_session_logs")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BreathingSessionLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technique_id", nullable = false)
    private BreathingTechnique technique;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "xp_awarded", nullable = false)
    private boolean xpAwarded = false;

    public BreathingSessionLog(User user, BreathingTechnique technique, int durationSeconds) {
        this.user = user;
        this.technique = technique;
        this.durationSeconds = durationSeconds;
    }
}
