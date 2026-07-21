package com.mib.backend.service.impl;

import com.mib.backend.dto.request.UpdateProfileRequest;
import com.mib.backend.dto.response.ProfileResponse;
import com.mib.backend.entity.FriendshipStatus;
import com.mib.backend.entity.Profile;
import com.mib.backend.entity.User;
import com.mib.backend.exception.ForbiddenProfileAccessException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.mapper.UserMapper;
import com.mib.backend.repository.FriendshipRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final AvatarStorageService avatarStorageService;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getOwnProfile(UUID currentUserId) {
        User user = findUserOrThrow(currentUserId);
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getPublicProfile(UUID targetUserId, UUID currentUserId) {
        User target = findUserOrThrow(targetUserId);
        Profile profile = target.getProfile();

        boolean isOwner = targetUserId.equals(currentUserId);
        boolean isPublic = profile == null || profile.isPublic();

        if (!isOwner && !isPublic) {
            boolean areFriends = friendshipRepository.findBetweenUsers(targetUserId, currentUserId)
                    .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                    .orElse(false);

            if (!areFriends) {
                throw new ForbiddenProfileAccessException("Este perfil e privado");
            }
        }

        return userMapper.toProfileResponse(target);
    }

    @Override
    @Transactional
    public ProfileResponse updateOwnProfile(UUID currentUserId, UpdateProfileRequest request) {
        User user = findUserOrThrow(currentUserId);
        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile(user, user.getUsername());
            user.setProfile(profile);
        }

        if (request.displayName() != null) {
            profile.setDisplayName(request.displayName());
        }
        if (request.bio() != null) {
            profile.setBio(request.bio());
        }
        if (request.statusMessage() != null) {
            profile.setStatusMessage(request.statusMessage());
        }
        if (request.isPublic() != null) {
            profile.setPublic(request.isPublic());
        }

        userRepository.save(user);
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public ProfileResponse updateAvatar(UUID currentUserId, MultipartFile file) {
        User user = findUserOrThrow(currentUserId);
        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile(user, user.getUsername());
            user.setProfile(profile);
        }

        String avatarUrl = avatarStorageService.store(currentUserId, file);
        profile.setAvatarUrl(avatarUrl);

        userRepository.save(user);
        return userMapper.toProfileResponse(user);
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
