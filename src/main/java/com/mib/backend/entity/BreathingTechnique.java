package com.mib.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tecnica de respiracao guiada. Os campos de segundos descrevem o padrao usado pelo
 * app para animar o ciclo inspirar/segurar/expirar/segurar (ex.: 4-7-8 usa
 * inhale=4, holdAfterInhale=7, exhale=8, holdAfterExhale=0).
 * <p>
 * Nota: "Box Breathing" e "Respiracao Quadrada" sao a mesma tecnica (o segundo e
 * apenas o nome em portugues do primeiro) — consolidadas aqui em um unico registro
 * para nao duplicar o mesmo padrao sob dois nomes.
 */
@Entity
@Table(name = "breathing_techniques")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BreathingTechnique extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 400)
    private String description;

    @Column(nullable = false, length = 400)
    private String benefits;

    @Column(name = "inhale_seconds", nullable = false)
    private int inhaleSeconds;

    @Column(name = "hold_after_inhale_seconds", nullable = false)
    private int holdAfterInhaleSeconds;

    @Column(name = "exhale_seconds", nullable = false)
    private int exhaleSeconds;

    @Column(name = "hold_after_exhale_seconds", nullable = false)
    private int holdAfterExhaleSeconds;

    /** Numero de ciclos sugerido para compor uma sessao completa. */
    @Column(name = "suggested_cycles", nullable = false)
    private int suggestedCycles;

    @Column(nullable = false)
    private boolean active = true;
}
