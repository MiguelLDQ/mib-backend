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

import java.time.Instant;

@Entity
@Table(name = "user_inventory_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "shop_item_id"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserInventoryItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_item_id", nullable = false)
    private ShopItem shopItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InventorySource source;

    @Column(nullable = false)
    private boolean equipped = false;

    @Column(name = "unlocked_at", nullable = false)
    private Instant unlockedAt = Instant.now();

    public UserInventoryItem(User user, ShopItem shopItem, InventorySource source) {
        this.user = user;
        this.shopItem = shopItem;
        this.source = source;
        this.unlockedAt = Instant.now();
    }
}
