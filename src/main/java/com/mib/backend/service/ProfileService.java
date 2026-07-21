package com.mib.backend.service;

import com.mib.backend.dto.request.UpdateProfileRequest;
import com.mib.backend.dto.response.ProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProfileService {

    ProfileResponse getOwnProfile(UUID currentUserId);

    ProfileResponse getPublicProfile(UUID targetUserId, UUID currentUserId);

    ProfileResponse updateOwnProfile(UUID currentUserId, UpdateProfileRequest request);

    ProfileResponse updateAvatar(UUID currentUserId, MultipartFile file);
}
