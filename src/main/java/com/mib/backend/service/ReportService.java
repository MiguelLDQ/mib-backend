package com.mib.backend.service;

import com.mib.backend.dto.response.ReportResponse;

import java.util.UUID;

public interface ReportService {

    ReportResponse createReport(UUID reporterId, String targetType, UUID targetId, String reason, String description);
}
