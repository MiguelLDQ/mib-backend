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

@Entity
@Table(name = "positive_feed_items")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PositiveFeedItem extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PositiveFeedItemType type;

    @Column(nullable = false, length = 400)
    private String content;

    @Column(length = 100)
    private String author;

    @Column(nullable = false)
    private boolean active = true;

    public PositiveFeedItem(PositiveFeedItemType type, String content, String author) {
        this.type = type;
        this.content = content;
        this.author = author;
    }
}
