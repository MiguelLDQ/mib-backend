package com.mib.backend.service;

import com.mib.backend.dto.response.AdminLogResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.entity.AdminActionType;
import com.mib.backend.entity.User;

import java.util.UUID;

public interface AdminAuditService {

    void log(User admin, AdminActionType action, String targetType, UUID targetId, String detail);

    PagedResponse<AdminLogResponse> list(int page, int size);
}
