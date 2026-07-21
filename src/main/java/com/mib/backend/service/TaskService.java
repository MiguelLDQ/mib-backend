package com.mib.backend.service;

import com.mib.backend.dto.request.CreateTaskRequest;
import com.mib.backend.dto.request.UpdateTaskRequest;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.dto.response.TaskResponse;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    PagedResponse<TaskResponse> search(UUID userId, String status, String category, int page, int size);

    TaskResponse getById(UUID userId, UUID taskId);

    TaskResponse create(UUID userId, CreateTaskRequest request);

    TaskResponse update(UUID userId, UUID taskId, UpdateTaskRequest request);

    TaskResponse complete(UUID userId, UUID taskId);

    TaskResponse reopen(UUID userId, UUID taskId);

    void delete(UUID userId, UUID taskId);

    List<String> listCategories(UUID userId);
}
