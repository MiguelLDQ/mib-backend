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

@Entity
@Table(name = "xp_history")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class XpHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private XpReasonType reason;

    @Column(length = 200)
    private String description;

    @Column(name = "level_after", nullable = false)
    private int levelAfter;

    public XpHistory(User user, int amount, XpReasonType reason, String description, int levelAfter) {
        this.user = user;
        this.amount = amount;
        this.reason = reason;
        this.description = description;
        this.levelAfter = levelAfter;
    }
}
