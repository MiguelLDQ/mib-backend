package com.mib.backend.repository;

import com.mib.backend.entity.PositiveFeedItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PositiveFeedItemRepository extends JpaRepository<PositiveFeedItem, UUID> {

    List<PositiveFeedItem> findAllByActiveTrue();
}
