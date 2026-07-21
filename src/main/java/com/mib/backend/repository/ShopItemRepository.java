package com.mib.backend.repository;

import com.mib.backend.entity.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShopItemRepository extends JpaRepository<ShopItem, UUID> {

    List<ShopItem> findAllByActiveTrueAndExclusiveToAchievementFalseOrderByTypeAscPriceXpAsc();
}
