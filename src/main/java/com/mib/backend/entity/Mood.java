package com.mib.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Registro diario de humor (um por usuario por dia — registrar de novo no mesmo dia
 * atualiza a entrada existente em vez de duplicar). Serve apenas como autorrelato do
 * usuario para gerar seu proprio grafico de evolucao emocional; nunca e usado para
 * fins de moderacao, restricao ou qualquer acao punitiva.
 */
@Entity
@Table(name = "moods", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "mood_date"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Mood extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood_level", nullable = false, length = 20)
    private MoodLevel moodLevel;

    @Column(length = 300)
    private String note;

    @Column(name = "mood_date", nullable = false)
    private LocalDate moodDate;

    public Mood(User user, MoodLevel moodLevel, String note, LocalDate moodDate) {
        this.user = user;
        this.moodLevel = moodLevel;
        this.note = note;
        this.moodDate = moodDate;
    }
}
