package com.mib.backend.service.impl;

import com.mib.backend.dto.response.FriendSuggestionResponse;
import com.mib.backend.entity.FriendshipStatus;
import com.mib.backend.repository.FriendshipRepository;
import com.mib.backend.repository.UserInterestRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.PresenceService;
import com.mib.backend.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final UserInterestRepository userInterestRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final PresenceService presenceService;

    @Override
    @Transactional(readOnly = true)
    public List<FriendSuggestionResponse> suggestFriends(UUID currentUserId, int limit) {
        return userInterestRepository.findUsersWithSharedInterests(currentUserId).stream()
                .filter(row -> isSuggestable(currentUserId, row.getUserId()))
                .limit(limit)
                .map(row -> toSuggestion(currentUserId, row))
                .toList();
    }

    private boolean isSuggestable(UUID currentUserId, UUID candidateUserId) {
        return friendshipRepository.findBetweenUsers(currentUserId, candidateUserId)
                .map(f -> f.getStatus() == FriendshipStatus.DECLINED) // ja recusada: pode voltar a ser sugerido
                .orElse(true); // nenhuma relacao ainda: sugestao valida
    }

    private FriendSuggestionResponse toSuggestion(UUID currentUserId, UserInterestRepository.SharedInterestCount row) {
        var user = userRepository.findById(row.getUserId()).orElseThrow();
        var profile = user.getProfile();
        boolean online = presenceService.isOnline(user.getId(), user.getLastActiveAt());

        String friendshipStatus = friendshipRepository.findBetweenUsers(currentUserId, user.getId())
                .map(f -> f.getStatus() == FriendshipStatus.PENDING
                        ? (f.getRequester().getId().equals(currentUserId) ? "PENDING_SENT" : "PENDING_RECEIVED")
                        : "NONE")
                .orElse("NONE");

        return new FriendSuggestionResponse(
                user.getId(), user.getUsername(),
                profile != null ? profile.getDisplayName() : user.getUsername(),
                profile != null ? profile.getAvatarUrl() : null,
                online, row.getSharedCount(), friendshipStatus);
    }
}
