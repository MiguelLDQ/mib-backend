package com.mib.backend.controller;

import com.mib.backend.dto.request.CreateTaskRequest;
import com.mib.backend.dto.request.UpdateTaskRequest;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.dto.response.TaskResponse;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Tarefas pessoais criadas pelo usuario")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Lista as tarefas do usuario, com filtros opcionais de status e categoria")
    public ResponseEntity<PagedResponse<TaskResponse>> search(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(taskService.search(SecurityUtils.currentUserId(), status, category, page, size));
    }

    @GetMapping("/categories")
    @Operation(summary = "Lista as categorias ja usadas pelo usuario, para preencher filtros/autocompletar")
    public ResponseEntity<List<String>> listCategories() {
        return ResponseEntity.ok(taskService.listCategories(SecurityUtils.currentUserId()));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Detalha uma tarefa do usuario")
    public ResponseEntity<TaskResponse> getById(@PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.getById(SecurityUtils.currentUserId(), taskId));
    }

    @PostMapping
    @Operation(summary = "Cria uma nova tarefa")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        var created = taskService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Atualiza titulo, descricao, categoria, vencimento e/ou status de uma tarefa")
    public ResponseEntity<TaskResponse> update(@PathVariable UUID taskId, @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.update(SecurityUtils.currentUserId(), taskId, request));
    }

    @PatchMapping("/{taskId}/complete")
    @Operation(summary = "Marca a tarefa como concluida e credita XP (uma unica vez por tarefa)")
    public ResponseEntity<TaskResponse> complete(@PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.complete(SecurityUtils.currentUserId(), taskId));
    }

    @PatchMapping("/{taskId}/reopen")
    @Operation(summary = "Reabre uma tarefa concluida (volta para em andamento, sem reverter o XP)")
    public ResponseEntity<TaskResponse> reopen(@PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.reopen(SecurityUtils.currentUserId(), taskId));
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Remove uma tarefa")
    public ResponseEntity<Void> delete(@PathVariable UUID taskId) {
        taskService.delete(SecurityUtils.currentUserId(), taskId);
        return ResponseEntity.noContent().build();
    }
}
