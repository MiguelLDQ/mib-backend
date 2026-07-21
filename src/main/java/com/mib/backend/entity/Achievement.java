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

/**
 * Modelo de conquista. O {@code code} e uma chave estavel usada no codigo
 * (ex.: "FIRST_LOGIN", "STREAK_7_DAYS") para conceder a conquista a partir de
 * qualquer modulo, sem depender do id gerado no banco.
 */
@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Achievement extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_shop_item_id")
    private ShopItem rewardShopItem;

    public Achievement(String code, String name, String description, int xpReward, ShopItem rewardShopItem) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.xpReward = xpReward;
        this.rewardShopItem = rewardShopItem;
    }
}
