package com.mib.backend.service.impl;

import com.mib.backend.dto.response.AnonymousMessageResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.entity.AnonymousMessage;
import com.mib.backend.entity.ContentTargetType;
import com.mib.backend.entity.User;
import com.mib.backend.entity.XpReasonType;
import com.mib.backend.exception.AnonymousMessageNotFoundException;
import com.mib.backend.exception.CannotLikeOwnMessageException;
import com.mib.backend.exception.MessageBlockedByModerationException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.mapper.AnonymousMessageMapper;
import com.mib.backend.repository.AnonymousMessageLikeRepository;
import com.mib.backend.repository.AnonymousMessageRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.AnonymousMessageService;
import com.mib.backend.service.ModerationService;
import com.mib.backend.service.XpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnonymousMessageServiceImpl implements AnonymousMessageService {

    /** XP concedido ao autor quando outro usuario curte sua mensagem. */
    private static final int XP_PER_LIKE_RECEIVED = 2;

    private final AnonymousMessageRepository messageRepository;
    private final AnonymousMessageLikeRepository likeRepository;
    private final UserRepository userRepository;
    private final AnonymousMessageMapper mapper;
    private final ModerationService moderationService;
    private final XpService xpService;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AnonymousMessageResponse> getFeed(UUID currentUserId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = messageRepository
                .findByParentMessageIsNullAndRemovedByModerationFalseAndRemovedByAuthorFalseOrderByCreatedAtDesc(pageable)
                .map(message -> toResponse(message, currentUserId));

        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnonymousMessageResponse> getReplies(UUID currentUserId, UUID parentMessageId) {
        return messageRepository
                .findByParentMessageIdAndRemovedByModerationFalseAndRemovedByAuthorFalseOrderByCreatedAtAsc(parentMessageId)
                .stream()
                .map(message -> toResponse(message, currentUserId))
                .toList();
    }

    @Override
    @Transactional
    public AnonymousMessageResponse create(UUID currentUserId, String content, UUID parentMessageId) {
        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        AnonymousMessage parent = null;
        if (parentMessageId != null) {
            parent = messageRepository.findById(parentMessageId)
                    .filter(AnonymousMessage::isVisible)
                    .orElseThrow(() -> new AnonymousMessageNotFoundException("Mensagem original nao encontrada"));
        }

        AnonymousMessage saved = messageRepository.save(new AnonymousMessage(author, parent, content.trim()));

        boolean removed = moderationService.moderate(author, ContentTargetType.ANONYMOUS_MESSAGE, saved.getId(), content);
        if (removed) {
            saved.setRemovedByModeration(true);
            messageRepository.save(saved);
            throw new MessageBlockedByModerationException(
                    "Sua mensagem foi removida automaticamente por violar as diretrizes da comunidade");
        }

        if (parent != null) {
            parent.setReplyCount(parent.getReplyCount() + 1);
            messageRepository.save(parent);
        }

        return toResponse(saved, currentUserId);
    }

    @Override
    @Transactional
    public void deleteOwnMessage(UUID currentUserId, UUID messageId) {
        AnonymousMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AnonymousMessageNotFoundException("Mensagem nao encontrada"));

        if (!message.getAuthor().getId().equals(currentUserId)) {
            throw new AnonymousMessageNotFoundException("Mensagem nao encontrada");
        }

        message.setRemovedByAuthor(true);
        messageRepository.save(message);
    }

    @Override
    @Transactional
    public boolean toggleLike(UUID currentUserId, UUID messageId) {
        AnonymousMessage message = messageRepository.findById(messageId)
                .filter(AnonymousMessage::isVisible)
                .orElseThrow(() -> new AnonymousMessageNotFoundException("Mensagem nao encontrada"));

        if (message.getAuthor().getId().equals(currentUserId)) {
            throw new CannotLikeOwnMessageException("Voce nao pode curtir a propria mensagem");
        }

        var existing = likeRepository.findByMessageIdAndUserId(messageId, currentUserId);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            message.setLikeCount(Math.max(0, message.getLikeCount() - 1));
            messageRepository.save(message);
            return false;
        }

        User liker = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        likeRepository.save(new com.mib.backend.entity.AnonymousMessageLike(message, liker));
        message.setLikeCount(message.getLikeCount() + 1);
        messageRepository.save(message);

        xpService.awardXp(message.getAuthor(), XP_PER_LIKE_RECEIVED, XpReasonType.LIKE_RECEIVED,
                "Curtida recebida em mensagem anonima");

        return true;
    }

    private AnonymousMessageResponse toResponse(AnonymousMessage message, UUID viewerUserId) {
        boolean likedByViewer = likeRepository.existsByMessageIdAndUserId(message.getId(), viewerUserId);
        return mapper.toResponse(message, viewerUserId, likedByViewer);
    }
}
