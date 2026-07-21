package com.mib.backend.service.impl;

import com.mib.backend.dto.request.CreateTaskRequest;
import com.mib.backend.dto.request.UpdateTaskRequest;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.dto.response.TaskResponse;
import com.mib.backend.entity.Task;
import com.mib.backend.entity.TaskStatus;
import com.mib.backend.entity.User;
import com.mib.backend.entity.XpReasonType;
import com.mib.backend.exception.BadRequestException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.exception.TaskNotFoundException;
import com.mib.backend.mapper.TaskMapper;
import com.mib.backend.repository.TaskRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.TaskService;
import com.mib.backend.service.XpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    /** XP concedido ao concluir uma tarefa pessoal (uma unica vez por tarefa). */
    private static final int XP_PER_TASK_COMPLETED = 15;

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final XpService xpService;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> search(UUID userId, String statusRaw, String category, int page, int size) {
        TaskStatus status = parseStatusOrNull(statusRaw);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        var result = taskRepository.search(userId, status, blankToNull(category), pageable)
                .map(taskMapper::toResponse);

        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getById(UUID userId, UUID taskId) {
        return taskMapper.toResponse(findOwnedTask(userId, taskId));
    }

    @Override
    @Transactional
    public TaskResponse create(UUID userId, CreateTaskRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        Task task = new Task(user, request.title().trim(),
                blankToNull(request.description()), blankToNull(request.category()), request.dueDate());

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse update(UUID userId, UUID taskId, UpdateTaskRequest request) {
        Task task = findOwnedTask(userId, taskId);

        if (request.title() != null && !request.title().isBlank()) {
            task.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            task.setDescription(blankToNull(request.description()));
        }
        if (request.category() != null) {
            task.setCategory(blankToNull(request.category()));
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }
        if (request.status() != null && !request.status().isBlank()) {
            applyStatusChange(task, parseStatus(request.status()));
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse complete(UUID userId, UUID taskId) {
        Task task = findOwnedTask(userId, taskId);
        applyStatusChange(task, TaskStatus.COMPLETED);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse reopen(UUID userId, UUID taskId) {
        Task task = findOwnedTask(userId, taskId);
        applyStatusChange(task, TaskStatus.IN_PROGRESS);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID taskId) {
        Task task = findOwnedTask(userId, taskId);
        taskRepository.delete(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listCategories(UUID userId) {
        return taskRepository.findDistinctCategoriesByUserId(userId);
    }

    private void applyStatusChange(Task task, TaskStatus newStatus) {
        boolean wasCompleted = task.getStatus() == TaskStatus.COMPLETED;
        task.setStatus(newStatus);

        if (newStatus == TaskStatus.COMPLETED) {
            task.setCompletedAt(Instant.now());
            if (!task.isXpAwarded()) {
                task.setXpAwarded(true);
                xpService.awardXp(task.getUser(), XP_PER_TASK_COMPLETED, XpReasonType.TASK_COMPLETED,
                        "Tarefa concluida: " + task.getTitle());
            }
        } else if (wasCompleted) {
            // Reabrir uma tarefa nao reverte o XP ja concedido; evita incentivo a "farmar"
            // XP marcando e desmarcando a mesma tarefa repetidamente.
            task.setCompletedAt(null);
        }
    }

    private Task findOwnedTask(UUID userId, UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Tarefa nao encontrada"));

        if (!task.getUser().getId().equals(userId)) {
            throw new TaskNotFoundException("Tarefa nao encontrada");
        }
        return task;
    }

    private TaskStatus parseStatusOrNull(String raw) {
        return (raw == null || raw.isBlank()) ? null : parseStatus(raw);
    }

    private TaskStatus parseStatus(String raw) {
        try {
            return TaskStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Status invalido: " + raw + " (use PENDING, IN_PROGRESS ou COMPLETED)");
        }
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
