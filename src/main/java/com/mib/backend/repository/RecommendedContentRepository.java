package com.mib.backend.repository;

import com.mib.backend.entity.ContentCategory;
import com.mib.backend.entity.RecommendedContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RecommendedContentRepository extends JpaRepository<RecommendedContent, UUID> {

    List<RecommendedContent> findByCategoryInAndActiveTrue(Set<ContentCategory> categories);

    List<RecommendedContent> findByActiveTrue();
}
