package com.mib.backend.repository;

import com.mib.backend.entity.Friendship;
import com.mib.backend.entity.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("""
            select f from Friendship f
            where f.status = :status
              and ((f.requester.id = :userId) or (f.addressee.id = :userId))
            """)
    List<Friendship> findAllByUserIdAndStatus(@Param("userId") UUID userId,
                                               @Param("status") FriendshipStatus status);

    @Query("""
            select f from Friendship f
            where f.addressee.id = :userId and f.status = :status
            """)
    List<Friendship> findIncomingByUserIdAndStatus(@Param("userId") UUID userId,
                                                     @Param("status") FriendshipStatus status);

    @Query("""
            select f from Friendship f
            where f.requester.id = :userId and f.status = :status
            """)
    List<Friendship> findOutgoingByUserIdAndStatus(@Param("userId") UUID userId,
                                                     @Param("status") FriendshipStatus status);

    @Query("""
            select f from Friendship f
            where (f.requester.id = :userA and f.addressee.id = :userB)
               or (f.requester.id = :userB and f.addressee.id = :userA)
            """)
    Optional<Friendship> findBetweenUsers(@Param("userA") UUID userA, @Param("userB") UUID userB);

    boolean existsByRequesterIdAndAddresseeIdAndStatus(UUID requesterId, UUID addresseeId, FriendshipStatus status);
}
