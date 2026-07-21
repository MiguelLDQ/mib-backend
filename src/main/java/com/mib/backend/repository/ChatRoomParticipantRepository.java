package com.mib.backend.repository;

import com.mib.backend.entity.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, UUID> {

    boolean existsByChatRoomIdAndUserId(UUID chatRoomId, UUID userId);

    List<ChatRoomParticipant> findAllByChatRoomId(UUID chatRoomId);

    @Query("""
            select p1.chatRoom.id from ChatRoomParticipant p1, ChatRoomParticipant p2
            where p1.chatRoom = p2.chatRoom
              and p1.chatRoom.type = com.mib.backend.entity.ChatRoomType.DIRECT
              and p1.user.id = :userA
              and p2.user.id = :userB
            """)
    Optional<UUID> findDirectRoomIdBetween(@Param("userA") UUID userA, @Param("userB") UUID userB);

    @Query("""
            select p.chatRoom.id from ChatRoomParticipant p
            where p.user.id = :userId and p.chatRoom.type = com.mib.backend.entity.ChatRoomType.DIRECT
            """)
    List<UUID> findAllDirectRoomIdsByUserId(@Param("userId") UUID userId);
}
