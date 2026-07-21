package com.mib.backend.repository;

import com.mib.backend.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserInterestRepository extends JpaRepository<UserInterest, UUID> {

    List<UserInterest> findAllByUserId(UUID userId);

    void deleteAllByUserId(UUID userId);

    /**
     * Para cada outro usuario que compartilha ao menos um interesse com {@code userId},
     * retorna o id dele e a quantidade de interesses em comum, do mais compativel para
     * o menos compativel. Base do algoritmo de recomendacao de amizades.
     */
    @Query("""
            select ui2.user.id as userId, count(ui2) as sharedCount
            from UserInterest ui1
            join UserInterest ui2 on ui2.interest.id = ui1.interest.id and ui2.user.id <> ui1.user.id
            where ui1.user.id = :userId
            group by ui2.user.id
            order by count(ui2) desc
            """)
    List<SharedInterestCount> findUsersWithSharedInterests(@Param("userId") UUID userId);

    interface SharedInterestCount {
        UUID getUserId();
        long getSharedCount();
    }
}
