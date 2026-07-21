package com.mib.backend.repository;

import com.mib.backend.entity.ContentTargetType;
import com.mib.backend.entity.Report;
import com.mib.backend.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    boolean existsByReporterIdAndTargetTypeAndTargetId(UUID reporterId, ContentTargetType targetType, UUID targetId);

    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    long countByStatus(ReportStatus status);
}
