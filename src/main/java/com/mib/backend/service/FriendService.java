package com.mib.backend.service;

import com.mib.backend.dto.response.FriendRequestResponse;
import com.mib.backend.dto.response.FriendSummary;
import com.mib.backend.dto.response.UserSearchResult;

import java.util.List;
import java.util.UUID;

public interface FriendService {

    List<UserSearchResult> searchUsers(UUID currentUserId, String query);

    List<FriendSummary> listFriends(UUID currentUserId);

    List<FriendRequestResponse> listIncomingRequests(UUID currentUserId);

    List<FriendRequestResponse> listOutgoingRequests(UUID currentUserId);

    void sendRequest(UUID currentUserId, UUID targetUserId);

    void acceptRequest(UUID currentUserId, UUID requestId);

    void declineRequest(UUID currentUserId, UUID requestId);

    void cancelRequest(UUID currentUserId, UUID requestId);

    void removeFriend(UUID currentUserId, UUID friendUserId);
}
