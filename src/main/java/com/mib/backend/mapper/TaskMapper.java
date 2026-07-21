package com.mib.backend.mapper;

import com.mib.backend.dto.response.TaskResponse;
import com.mib.backend.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCategory(),
                task.getStatus().name(),
                task.getDueDate(),
                task.getCompletedAt(),
                task.getCreatedAt()
        );
    }
}
