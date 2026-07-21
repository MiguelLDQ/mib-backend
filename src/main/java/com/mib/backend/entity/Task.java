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

import java.time.Instant;
import java.time.LocalDate;

/**
 * Tarefa pessoal criada livremente pelo usuario (categoria e um texto livre definido
 * por ele mesmo, ex.: "Estudos", "Saude", "Trabalho"), distinta das {@link DailyMission}
 * geradas automaticamente pelo sistema.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Task extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_at")
    private Instant completedAt;

    /** Garante que o XP de conclusao so seja concedido uma vez por tarefa. */
    @Column(name = "xp_awarded", nullable = false)
    private boolean xpAwarded = false;

    public Task(User user, String title, String description, String category, LocalDate dueDate) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.category = category;
        this.dueDate = dueDate;
    }
}
