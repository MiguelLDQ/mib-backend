package com.mib.backend.repository;

import com.mib.backend.entity.AdminLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminLogRepository extends JpaRepository<AdminLog, UUID> {

    Page<AdminLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
