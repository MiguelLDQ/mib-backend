package com.mib.backend.repository;

import com.mib.backend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    @Query("""
            select u from User u left join u.profile p
            where u.id <> :excludeUserId
              and (lower(u.username) like lower(concat('%', :query, '%'))
                   or lower(p.displayName) like lower(concat('%', :query, '%')))
            order by u.username
            """)
    List<User> searchByUsernameOrDisplayName(@Param("query") String query,
                                              @Param("excludeUserId") UUID excludeUserId,
                                              Pageable pageable);

    @Query("""
            select u from User u left join u.profile p
            where (:query is null or :query = ''
                   or lower(u.username) like lower(concat('%', :query, '%'))
                   or lower(u.email) like lower(concat('%', :query, '%'))
                   or lower(p.displayName) like lower(concat('%', :query, '%')))
            order by u.createdAt desc
            """)
    org.springframework.data.domain.Page<User> searchForAdmin(@Param("query") String query,
                                                                Pageable pageable);

    long countByBannedTrue();

    long countBySuspendedUntilAfter(java.time.Instant now);

    long countByCreatedAtAfter(java.time.Instant since);
}
