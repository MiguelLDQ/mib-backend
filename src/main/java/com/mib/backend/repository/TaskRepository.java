package com.mib.backend.repository;

import com.mib.backend.entity.Task;
import com.mib.backend.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    // Correcao: o parametro :category precisava de um CAST explicito para texto.
    // Quando ele chega null (nenhum filtro de categoria aplicado, ex.: GET /api/tasks
    // sem query string), o driver JDBC nao consegue inferir o tipo do parametro dentro
    // de lower(:category) e o Postgres assume "bytea" por padrao, quebrando com
    // "function lower(bytea) does not exist". O cast resolve a ambiguidade mesmo com
    // valor nulo.
    @Query("""
            select t from Task t
            where t.user.id = :userId
              and (:status is null or t.status = :status)
              and (:category is null or lower(t.category) = lower(cast(:category as string)))
            order by t.createdAt desc
            """)
    Page<Task> search(@Param("userId") UUID userId,
                      @Param("status") TaskStatus status,
                      @Param("category") String category,
                      Pageable pageable);

    @Query("""
            select distinct t.category from Task t
            where t.user.id = :userId and t.category is not null
            order by t.category
            """)
    List<String> findDistinctCategoriesByUserId(@Param("userId") UUID userId);

    long countByStatus(TaskStatus status);
}