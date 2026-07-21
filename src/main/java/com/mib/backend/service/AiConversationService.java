package com.mib.backend.service;

import com.mib.backend.dto.response.AiMessageResponse;
import com.mib.backend.dto.response.PagedResponse;

import java.util.UUID;

public interface AiConversationService {

    PagedResponse<AiMessageResponse> getHistory(UUID userId, int page, int size);

    AiMessageResponse sendMessage(UUID userId, String content);

    void clearHistory(UUID userId);
}
