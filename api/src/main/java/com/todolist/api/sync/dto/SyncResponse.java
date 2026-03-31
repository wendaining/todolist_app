package com.todolist.api.sync.dto;

import com.todolist.api.task.dto.TaskResponse;

import java.time.OffsetDateTime;
import java.util.List;

public record SyncResponse(
        List<TaskResponse> tasks,
        OffsetDateTime serverTime
) {
}
