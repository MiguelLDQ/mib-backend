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
 * Item cosmetico da loja de personalizacao. Itens com {@code exclusiveToAchievement=true}
 * nao aparecem para compra — sao concedidos automaticamente quando a conquista
 * correspondente e desbloqueada (ver {@link Achievement#getRewardShopItem()}).
 */
@Entity
@Table(name = "shop_items")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ShopItem extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShopItemType type;

    @Column(name = "price_xp", nullable = false)
    private int priceXp;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "exclusive_to_achievement", nullable = false)
    private boolean exclusiveToAchievement = false;

    public ShopItem(String name, String description, ShopItemType type, int priceXp,
                     String iconUrl, boolean exclusiveToAchievement) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.priceXp = priceXp;
        this.iconUrl = iconUrl;
        this.exclusiveToAchievement = exclusiveToAchievement;
    }
}
