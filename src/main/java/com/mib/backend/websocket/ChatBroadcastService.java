package com.mib.backend.websocket;

import com.mib.backend.dto.response.MessageResponse;
import com.mib.backend.entity.ChatRoom;
import com.mib.backend.entity.ChatRoomType;
import com.mib.backend.exception.ChatRoomNotFoundException;
import com.mib.backend.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatBroadcastService {

    private static final String GENERAL_TOPIC = "/topic/chat.general";
    private static final String DIRECT_ROOM_TOPIC_PREFIX = "/topic/chat.room.";

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;

    public void broadcast(MessageResponse message) {
        ChatRoom room = chatRoomRepository.findById(message.chatRoomId())
                .orElseThrow(() -> new ChatRoomNotFoundException("Sala de chat nao encontrada"));

        String destination = room.getType() == ChatRoomType.GENERAL
                ? GENERAL_TOPIC
                : DIRECT_ROOM_TOPIC_PREFIX + room.getId();

        messagingTemplate.convertAndSend(destination, message);
    }
}
