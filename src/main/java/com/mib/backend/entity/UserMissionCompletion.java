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
@Table(name = "user_mission_completions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "daily_mission_id"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserMissionCompletion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_mission_id", nullable = false)
    private DailyMission dailyMission;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    public UserMissionCompletion(User user, DailyMission dailyMission) {
        this.user = user;
        this.dailyMission = dailyMission;
        this.completedAt = Instant.now();
    }
}
