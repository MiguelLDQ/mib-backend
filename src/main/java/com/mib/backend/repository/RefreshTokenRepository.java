package com.mib.backend.repository;

import com.mib.backend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("update RefreshToken r set r.revoked = true where r.user.id = :userId and r.revoked = false")
    void revokeAllByUserId(UUID userId);
}
