package com.mib.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Vínculo entre um usuário e um item do catálogo, gerado pelo algoritmo
 * de recomendação (contexto do usuário + ranking via IA).
 */
@Entity
@Table(name = "user_content_recommendation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserContentRecommendation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private RecommendedContent content;

    @Column(nullable = false)
    private double relevanceScore;

    @Column(length = 500)
    private String aiReason;

    @Builder.Default
    @Column(nullable = false)
    private boolean shown = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean clicked = false;

    @Column(nullable = false)
    private LocalDate recommendedFor;
}
