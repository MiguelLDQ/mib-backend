package com.mib.backend.service;

import com.mib.backend.dto.response.AdminUserResponse;
import com.mib.backend.dto.response.PagedResponse;

import java.util.UUID;

public interface AdminUserService {

    PagedResponse<AdminUserResponse> search(String query, int page, int size);

    AdminUserResponse getById(UUID userId);

    AdminUserResponse suspend(UUID adminId, UUID targetUserId, int hours, String reason);

    AdminUserResponse unsuspend(UUID adminId, UUID targetUserId);

    AdminUserResponse ban(UUID adminId, UUID targetUserId, String reason);

    AdminUserResponse unban(UUID adminId, UUID targetUserId);

    AdminUserResponse grantAdminRole(UUID adminId, UUID targetUserId);

    AdminUserResponse revokeAdminRole(UUID adminId, UUID targetUserId);
}
