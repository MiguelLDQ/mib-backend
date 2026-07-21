package com.mib.backend.entity;

import jakarta.persistence.Column;
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

import java.time.Instant;

@Entity
@Table(name = "user_achievements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserAchievement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(name = "unlocked_at", nullable = false)
    private Instant unlockedAt = Instant.now();

    public UserAchievement(User user, Achievement achievement) {
        this.user = user;
        this.achievement = achievement;
        this.unlockedAt = Instant.now();
    }
}
