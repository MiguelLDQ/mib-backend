package com.mib.backend.service.impl;

import com.mib.backend.dto.response.DirectRoomResponse;
import com.mib.backend.dto.response.MessageResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.entity.ChatRoom;
import com.mib.backend.entity.ChatRoomParticipant;
import com.mib.backend.entity.ChatRoomType;
import com.mib.backend.entity.ContentTargetType;
import com.mib.backend.entity.FriendshipStatus;
import com.mib.backend.entity.Message;
import com.mib.backend.entity.NotificationType;
import com.mib.backend.entity.User;
import com.mib.backend.exception.ChatRoomAccessDeniedException;
import com.mib.backend.exception.ChatRoomNotFoundException;
import com.mib.backend.exception.MessageBlockedByModerationException;
import com.mib.backend.exception.NotFriendsException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.mapper.ChatMapper;
import com.mib.backend.repository.ChatRoomParticipantRepository;
import com.mib.backend.repository.ChatRoomRepository;
import com.mib.backend.repository.FriendshipRepository;
import com.mib.backend.repository.MessageRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.ChatService;
import com.mib.backend.service.ModerationService;
import com.mib.backend.service.NotificationService;
import com.mib.backend.service.PresenceService;
import com.mib.backend.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final ChatMapper chatMapper;
    private final PresenceService presenceService;
    private final ModerationService moderationService;
    private final AchievementService achievementService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public UUID getOrCreateGeneralRoomId() {
        return chatRoomRepository.findFirstByType(ChatRoomType.GENERAL)
                .map(ChatRoom::getId)
                .orElseGet(() -> chatRoomRepository.save(new ChatRoom(ChatRoomType.GENERAL, "Sala Geral")).getId());
    }

    @Override
    @Transactional
    public DirectRoomResponse getOrCreateDirectRoom(UUID currentUserId, UUID friendUserId) {
        if (currentUserId.equals(friendUserId)) {
            throw new ChatRoomAccessDeniedException("Voce nao pode iniciar uma conversa consigo mesmo");
        }

        boolean areFriends = friendshipRepository.findBetweenUsers(currentUserId, friendUserId)
                .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .orElse(false);
        if (!areFriends) {
            throw new NotFriendsException("Voce so pode conversar em privado com amigos");
        }

        UUID roomId = participantRepository.findDirectRoomIdBetween(currentUserId, friendUserId)
                .orElseGet(() -> createDirectRoom(currentUserId, friendUserId));

        return toDirectRoomResponse(roomId, friendUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DirectRoomResponse> listDirectRooms(UUID currentUserId) {
        return participantRepository.findAllDirectRoomIdsByUserId(currentUserId).stream()
                .map(roomId -> {
                    UUID friendId = participantRepository.findAllByChatRoomId(roomId).stream()
                            .map(p -> p.getUser().getId())
                            .filter(id -> !id.equals(currentUserId))
                            .findFirst()
                            .orElse(null);
                    return friendId != null ? toDirectRoomResponse(roomId, friendId) : null;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MessageResponse> getRoomHistory(UUID currentUserId, UUID roomId, int page, int size) {
        assertCanAccessRoom(currentUserId, roomId);

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = messageRepository
                .findByChatRoomIdAndRemovedByModerationFalseOrderByCreatedAtDesc(roomId, pageable)
                .map(chatMapper::toResponse);

        return PagedResponse.from(result);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(UUID senderId, UUID roomId, String content) {
        assertCanAccessRoom(senderId, roomId);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Sala de chat nao encontrada"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        Message saved = messageRepository.save(new Message(chatRoom, sender, content.trim()));

        boolean removed = moderationService.moderate(sender, ContentTargetType.CHAT_MESSAGE, saved.getId(), content);
        if (removed) {
            saved.setRemovedByModeration(true);
            messageRepository.save(saved);
            throw new MessageBlockedByModerationException(
                    "Sua mensagem foi removida automaticamente por violar as diretrizes da comunidade");
        }

        checkMessageMilestone(sender);
        notifyDirectMessageIfApplicable(chatRoom, sender);

        return chatMapper.toResponse(saved);
    }

    private void notifyDirectMessageIfApplicable(ChatRoom chatRoom, User sender) {
        if (chatRoom.getType() != ChatRoomType.DIRECT) {
            return; // notificacao de nova mensagem so faz sentido para conversas privadas
        }

        participantRepository.findAllByChatRoomId(chatRoom.getId()).stream()
                .map(ChatRoomParticipant::getUser)
                .filter(participant -> !participant.getId().equals(sender.getId()))
                .findFirst()
                .ifPresent(recipient -> notificationService.notify(recipient, NotificationType.NEW_MESSAGE,
                        "Nova mensagem",
                        sender.getUsername() + " enviou uma mensagem para voce",
                        "CHAT_ROOM", chatRoom.getId()));
    }

    private void checkMessageMilestone(User sender) {
        long total = messageRepository.countBySenderIdAndRemovedByModerationFalse(sender.getId());
        achievementService.checkMessageMilestones(sender, total);
    }

    @Override
    @Transactional(readOnly = true)
    public void assertCanAccessRoom(UUID userId, UUID roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Sala de chat nao encontrada"));

        if (room.getType() == ChatRoomType.GENERAL) {
            return; // sala geral e aberta a qualquer usuario autenticado
        }

        boolean isParticipant = participantRepository.existsByChatRoomIdAndUserId(roomId, userId);
        if (!isParticipant) {
            throw new ChatRoomAccessDeniedException("Voce nao tem acesso a esta conversa");
        }
    }

    private UUID createDirectRoom(UUID userAId, UUID userBId) {
        User userA = userRepository.findById(userAId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
        User userB = userRepository.findById(userBId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        ChatRoom room = chatRoomRepository.save(new ChatRoom(ChatRoomType.DIRECT, null));
        participantRepository.save(new ChatRoomParticipant(room, userA));
        participantRepository.save(new ChatRoomParticipant(room, userB));

        return room.getId();
    }

    private DirectRoomResponse toDirectRoomResponse(UUID roomId, UUID friendUserId) {
        User friend = userRepository.findById(friendUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
        var profile = friend.getProfile();
        boolean online = presenceService.isOnline(friend.getId(), friend.getLastActiveAt());

        return new DirectRoomResponse(
                roomId,
                friend.getId(),
                friend.getUsername(),
                profile != null ? profile.getDisplayName() : friend.getUsername(),
                profile != null ? profile.getAvatarUrl() : null,
                online
        );
    }
}
