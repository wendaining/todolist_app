package com.todolist.api.task.dto;

import com.todolist.api.task.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

public record CreateTaskRequest(
        @NotBlank(message = "title must not be blank")
        String title,
        TaskPriority priority,
        OffsetDateTime dueAt
) {
}
