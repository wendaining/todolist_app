package com.todolist.api.sync.dto;

import com.todolist.api.task.model.Task;
import com.todolist.api.task.model.TaskPriority;
import com.todolist.api.task.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record SyncTaskPayload(
        @NotBlank(message = "id must not be blank")
        String id,
        @NotBlank(message = "title must not be blank")
        String title,
        @NotNull(message = "status is required")
        TaskStatus status,
        @NotNull(message = "priority is required")
        TaskPriority priority,
        OffsetDateTime dueAt,
        @NotNull(message = "createdAt is required")
        OffsetDateTime createdAt,
        @NotNull(message = "updatedAt is required")
        OffsetDateTime updatedAt,
        OffsetDateTime completedAt
) {

    public Task toTask() {
        return new Task(
                id,
                title,
                status,
                priority,
                dueAt,
                createdAt,
                updatedAt,
                completedAt
        );
    }
}
