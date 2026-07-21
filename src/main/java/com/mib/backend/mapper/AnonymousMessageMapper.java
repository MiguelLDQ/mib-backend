package com.mib.backend.mapper;

import com.mib.backend.dto.response.AnonymousMessageResponse;
import com.mib.backend.entity.AnonymousMessage;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AnonymousMessageMapper {

    public AnonymousMessageResponse toResponse(AnonymousMessage message, UUID viewerUserId, boolean likedByViewer) {
        return new AnonymousMessageResponse(
                message.getId(),
                message.getParentMessage() != null ? message.getParentMessage().getId() : null,
                message.getContent(),
                message.getLikeCount(),
                message.getReplyCount(),
                likedByViewer,
                message.getAuthor().getId().equals(viewerUserId),
                message.getCreatedAt()
        );
    }
}
