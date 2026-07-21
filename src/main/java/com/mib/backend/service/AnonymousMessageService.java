package com.mib.backend.service;

import com.mib.backend.dto.response.AnonymousMessageResponse;
import com.mib.backend.dto.response.PagedResponse;

import java.util.List;
import java.util.UUID;

public interface AnonymousMessageService {

    PagedResponse<AnonymousMessageResponse> getFeed(UUID currentUserId, int page, int size);

    List<AnonymousMessageResponse> getReplies(UUID currentUserId, UUID parentMessageId);

    AnonymousMessageResponse create(UUID currentUserId, String content, UUID parentMessageId);

    void deleteOwnMessage(UUID currentUserId, UUID messageId);

    boolean toggleLike(UUID currentUserId, UUID messageId);
}
