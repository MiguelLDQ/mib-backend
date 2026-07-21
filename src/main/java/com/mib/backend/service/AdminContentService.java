package com.mib.backend.service;

import com.mib.backend.dto.response.ModerationLogResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.dto.response.ReportResponse;

import java.util.UUID;

public interface AdminContentService {

    void deleteChatMessage(UUID adminId, UUID messageId);

    void deleteAnonymousMessage(UUID adminId, UUID messageId);

    PagedResponse<ReportResponse> listReports(String status, int page, int size);

    ReportResponse reviewReport(UUID adminId, UUID reportId, String status);

    PagedResponse<ModerationLogResponse> listModerationLogs(int page, int size);
}
