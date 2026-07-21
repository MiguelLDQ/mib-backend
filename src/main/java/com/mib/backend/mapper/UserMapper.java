package com.mib.backend.mapper;

import com.mib.backend.dto.response.AuthResponse;
import com.mib.backend.dto.response.EquippedItemSummary;
import com.mib.backend.dto.response.FriendSummary;
import com.mib.backend.dto.response.ProfileResponse;
import com.mib.backend.dto.response.UserSearchResult;
import com.mib.backend.entity.Profile;
import com.mib.backend.entity.User;
import com.mib.backend.repository.UserInventoryItemRepository;
import com.mib.backend.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PresenceService presenceService;
    private final UserInventoryItemRepository inventoryItemRepository;

    public AuthResponse.UserSummary toUserSummary(User user) {
        String displayName = (user.getProfile() != null) ? user.getProfile().getDisplayName() : user.getUsername();
        return new AuthResponse.UserSummary(user.getId(), user.getUsername(), user.getEmail(), displayName);
    }

    public ProfileResponse toProfileResponse(User user) {
        Profile profile = user.getProfile();
        boolean online = presenceService.isOnline(user.getId(), user.getLastActiveAt());
        var lastSeen = presenceService.lastSeen(user.getId(), user.getLastActiveAt());

        var equippedItems = inventoryItemRepository.findAllByUserIdAndEquippedTrue(user.getId()).stream()
                .map(item -> new EquippedItemSummary(
                        item.getShopItem().getType().name(), item.getShopItem().getName(), item.getShopItem().getIconUrl()))
                .toList();

        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                profile != null ? profile.getDisplayName() : user.getUsername(),
                profile != null ? profile.getBio() : null,
                profile != null ? profile.getAvatarUrl() : null,
                profile != null ? profile.getStatusMessage() : null,
                profile == null || profile.isPublic(),
                online,
                lastSeen,
                user.getCreatedAt(),
                equippedItems
        );
    }

    public UserSearchResult toSearchResult(User user, String friendshipStatus) {
        Profile profile = user.getProfile();
        boolean online = presenceService.isOnline(user.getId(), user.getLastActiveAt());

        return new UserSearchResult(
                user.getId(),
                user.getUsername(),
                profile != null ? profile.getDisplayName() : user.getUsername(),
                profile != null ? profile.getAvatarUrl() : null,
                online,
                friendshipStatus
        );
    }

    public FriendSummary toFriendSummary(User friend, java.time.Instant friendsSince) {
        Profile profile = friend.getProfile();
        boolean online = presenceService.isOnline(friend.getId(), friend.getLastActiveAt());
        var lastSeen = presenceService.lastSeen(friend.getId(), friend.getLastActiveAt());

        return new FriendSummary(
                friend.getId(),
                friend.getUsername(),
                profile != null ? profile.getDisplayName() : friend.getUsername(),
                profile != null ? profile.getAvatarUrl() : null,
                profile != null ? profile.getStatusMessage() : null,
                online,
                lastSeen,
                friendsSince
        );
    }
}
