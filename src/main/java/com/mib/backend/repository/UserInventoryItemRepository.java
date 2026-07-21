package com.mib.backend.repository;

import com.mib.backend.entity.ShopItemType;
import com.mib.backend.entity.UserInventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserInventoryItemRepository extends JpaRepository<UserInventoryItem, UUID> {

    List<UserInventoryItem> findAllByUserIdOrderByUnlockedAtDesc(UUID userId);

    Optional<UserInventoryItem> findByUserIdAndShopItemId(UUID userId, UUID shopItemId);

    boolean existsByUserIdAndShopItemId(UUID userId, UUID shopItemId);

    List<UserInventoryItem> findAllByUserIdAndEquippedTrue(UUID userId);

    @Query("""
            select i from UserInventoryItem i
            where i.user.id = :userId and i.shopItem.type = :type and i.equipped = true
            """)
    List<UserInventoryItem> findEquippedByUserIdAndType(@Param("userId") UUID userId, @Param("type") ShopItemType type);
}
