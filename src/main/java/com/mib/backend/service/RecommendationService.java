package com.mib.backend.service;

import com.mib.backend.dto.response.FriendSuggestionResponse;

import java.util.List;
import java.util.UUID;

public interface RecommendationService {

    /** Sugere pessoas com interesses em comum, ordenadas pela maior compatibilidade,
     * excluindo quem ja e amigo ou tem uma solicitacao pendente com o usuario. */
    List<FriendSuggestionResponse> suggestFriends(UUID currentUserId, int limit);
}
