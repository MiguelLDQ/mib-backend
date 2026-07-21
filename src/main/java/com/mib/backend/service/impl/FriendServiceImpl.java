package com.mib.backend.service.impl;

import com.mib.backend.dto.response.FriendRequestResponse;
import com.mib.backend.dto.response.FriendSummary;
import com.mib.backend.dto.response.UserSearchResult;
import com.mib.backend.entity.Friendship;
import com.mib.backend.entity.FriendshipStatus;
import com.mib.backend.entity.NotificationType;
import com.mib.backend.entity.User;
import com.mib.backend.exception.CannotFriendSelfException;
import com.mib.backend.exception.FriendRequestNotFoundException;
import com.mib.backend.exception.FriendshipAlreadyExistsException;
import com.mib.backend.exception.NotFriendsException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.mapper.UserMapper;
import com.mib.backend.repository.FriendshipRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.AchievementCodes;
import com.mib.backend.service.AchievementService;
import com.mib.backend.service.FriendService;
import com.mib.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private static final int SEARCH_RESULT_LIMIT = 20;

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserMapper userMapper;
    private final AchievementService achievementService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchResult> searchUsers(UUID currentUserId, String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        List<User> results = userRepository.searchByUsernameOrDisplayName(
                query.trim(), currentUserId, PageRequest.of(0, SEARCH_RESULT_LIMIT));

        return results.stream()
                .map(user -> userMapper.toSearchResult(user, resolveFriendshipStatus(currentUserId, user.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendSummary> listFriends(UUID currentUserId) {
        return friendshipRepository.findAllByUserIdAndStatus(currentUserId, FriendshipStatus.ACCEPTED).stream()
                .map(friendship -> {
                    User other = otherUser(friendship, currentUserId);
                    Instant since = friendship.getRespondedAt() != null
                            ? friendship.getRespondedAt() : friendship.getCreatedAt();
                    return userMapper.toFriendSummary(other, since);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendRequestResponse> listIncomingRequests(UUID currentUserId) {
        return friendshipRepository.findIncomingByUserIdAndStatus(currentUserId, FriendshipStatus.PENDING).stream()
                .map(f -> toRequestResponse(f, f.getRequester()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendRequestResponse> listOutgoingRequests(UUID currentUserId) {
        return friendshipRepository.findOutgoingByUserIdAndStatus(currentUserId, FriendshipStatus.PENDING).stream()
                .map(f -> toRequestResponse(f, f.getAddressee()))
                .toList();
    }

    @Override
    @Transactional
    public void sendRequest(UUID currentUserId, UUID targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new CannotFriendSelfException("Voce nao pode enviar uma solicitacao de amizade para si mesmo");
        }

        User requester = findUserOrThrow(currentUserId);
        User addressee = findUserOrThrow(targetUserId);

        friendshipRepository.findBetweenUsers(currentUserId, targetUserId).ifPresent(existing -> {
            if (existing.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new FriendshipAlreadyExistsException("Voces ja sao amigos");
            }
            if (existing.getStatus() == FriendshipStatus.PENDING) {
                throw new FriendshipAlreadyExistsException("Ja existe uma solicitacao pendente entre voces");
            }
        });

        Friendship saved = friendshipRepository.save(new Friendship(requester, addressee));

        notificationService.notify(addressee, NotificationType.FRIEND_REQUEST_RECEIVED,
                "Nova solicitacao de amizade",
                requester.getUsername() + " quer ser seu amigo no MIB",
                "FRIENDSHIP", saved.getId());
    }

    @Override
    @Transactional
    public void acceptRequest(UUID currentUserId, UUID requestId) {
        Friendship friendship = findPendingRequestReceivedBy(currentUserId, requestId);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setRespondedAt(Instant.now());
        friendshipRepository.save(friendship);

        achievementService.grant(friendship.getRequester(), AchievementCodes.FIRST_FRIEND);
        achievementService.grant(friendship.getAddressee(), AchievementCodes.FIRST_FRIEND);

        notificationService.notify(friendship.getRequester(), NotificationType.FRIEND_REQUEST_ACCEPTED,
                "Solicitacao aceita",
                friendship.getAddressee().getUsername() + " aceitou sua solicitacao de amizade",
                "FRIENDSHIP", friendship.getId());
    }

    @Override
    @Transactional
    public void declineRequest(UUID currentUserId, UUID requestId) {
        Friendship friendship = findPendingRequestReceivedBy(currentUserId, requestId);
        friendship.setStatus(FriendshipStatus.DECLINED);
        friendship.setRespondedAt(Instant.now());
        friendshipRepository.save(friendship);
    }

    @Override
    @Transactional
    public void cancelRequest(UUID currentUserId, UUID requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .filter(f -> f.getStatus() == FriendshipStatus.PENDING)
                .filter(f -> f.getRequester().getId().equals(currentUserId))
                .orElseThrow(() -> new FriendRequestNotFoundException("Solicitacao nao encontrada"));

        friendshipRepository.delete(friendship);
    }

    @Override
    @Transactional
    public void removeFriend(UUID currentUserId, UUID friendUserId) {
        Friendship friendship = friendshipRepository.findBetweenUsers(currentUserId, friendUserId)
                .filter(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .orElseThrow(() -> new NotFriendsException("Voces nao sao amigos"));

        friendshipRepository.delete(friendship);
    }

    private Friendship findPendingRequestReceivedBy(UUID currentUserId, UUID requestId) {
        return friendshipRepository.findById(requestId)
                .filter(f -> f.getStatus() == FriendshipStatus.PENDING)
                .filter(f -> f.getAddressee().getId().equals(currentUserId))
                .orElseThrow(() -> new FriendRequestNotFoundException("Solicitacao nao encontrada"));
    }

    private String resolveFriendshipStatus(UUID currentUserId, UUID otherUserId) {
        return friendshipRepository.findBetweenUsers(currentUserId, otherUserId)
                .map(f -> switch (f.getStatus()) {
                    case ACCEPTED -> "FRIENDS";
                    case PENDING -> f.getRequester().getId().equals(currentUserId) ? "PENDING_SENT" : "PENDING_RECEIVED";
                    case DECLINED -> "NONE";
                })
                .orElse("NONE");
    }

    private User otherUser(Friendship friendship, UUID currentUserId) {
        return friendship.getRequester().getId().equals(currentUserId)
                ? friendship.getAddressee() : friendship.getRequester();
    }

    private FriendRequestResponse toRequestResponse(Friendship friendship, User counterpart) {
        var profile = counterpart.getProfile();
        return new FriendRequestResponse(
                friendship.getId(),
                counterpart.getId(),
                counterpart.getUsername(),
                profile != null ? profile.getDisplayName() : counterpart.getUsername(),
                profile != null ? profile.getAvatarUrl() : null,
                friendship.getCreatedAt()
        );
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
