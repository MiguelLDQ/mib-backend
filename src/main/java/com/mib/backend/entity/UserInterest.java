package com.mib.backend.entity;

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

@Entity
@Table(name = "user_interests",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "interest_id"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserInterest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    public UserInterest(User user, Interest interest) {
        this.user = user;
        this.interest = interest;
    }
}
