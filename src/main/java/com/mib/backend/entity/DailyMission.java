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

import java.time.LocalDate;

/**
 * Instancia diaria de uma missao, gerada automaticamente pelo scheduler a partir de um
 * {@link MissionTemplate}. E global (a mesma para todos os usuarios naquele dia); a
 * conclusao individual e registrada em {@link UserMissionCompletion}.
 */
@Entity
@Table(name = "daily_missions")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DailyMission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_template_id", nullable = false)
    private MissionTemplate template;

    @Column(name = "mission_date", nullable = false)
    private LocalDate missionDate;

    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    public DailyMission(MissionTemplate template, LocalDate missionDate, int xpReward) {
        this.template = template;
        this.missionDate = missionDate;
        this.xpReward = xpReward;
    }
}
