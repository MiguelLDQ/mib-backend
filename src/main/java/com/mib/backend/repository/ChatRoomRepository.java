package com.mib.backend.repository;

import com.mib.backend.entity.ChatRoom;
import com.mib.backend.entity.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    Optional<ChatRoom> findFirstByType(ChatRoomType type);
}
