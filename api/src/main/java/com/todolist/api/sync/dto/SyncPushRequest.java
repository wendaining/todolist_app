package com.todolist.api.sync.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SyncPushRequest(
        @NotNull(message = "tasks is required")
        List<@Valid SyncTaskPayload> tasks
) {
}
