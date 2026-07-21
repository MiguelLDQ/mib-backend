package com.mib.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo reutilizavel de missao (ex.: "Beba 2 litros de agua"). O scheduler diario
 * sorteia templates ativos para gerar as {@link DailyMission} do dia.
 */
@Entity
@Table(name = "mission_templates")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MissionTemplate extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MissionCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MissionDifficulty difficulty;

    @Column(name = "base_xp_reward", nullable = false)
    private int baseXpReward;

    @Column(nullable = false)
    private boolean active = true;

    public MissionTemplate(String title, String description, MissionCategory category,
                            MissionDifficulty difficulty, int baseXpReward) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.baseXpReward = baseXpReward;
    }
}
