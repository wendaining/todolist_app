package com.todolist.api.sync.service;

import com.todolist.api.sync.dto.SyncTaskPayload;
import com.todolist.api.task.model.Task;
import com.todolist.api.task.model.TaskPriority;
import com.todolist.api.task.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyncServiceTest {

    @Test
    void push_shouldApplyLwwMergeByUpdatedAt() {
        SyncService service = new SyncService();

        service.push("token-a", List.of(payload("task-1", "old", "2026-03-31T03:00:00Z")));
        service.push("token-a", List.of(payload("task-1", "new", "2026-03-31T04:00:00Z")));

        List<Task> tasks = service.pull("token-a");
        assertEquals(1, tasks.size());
        assertEquals("new", tasks.getFirst().getTitle());
        assertEquals(OffsetDateTime.parse("2026-03-31T04:00:00Z"), tasks.getFirst().getUpdatedAt());
    }

    @Test
    void pull_shouldIsolateTasksByToken() {
        SyncService service = new SyncService();

        service.push("token-a", List.of(payload("task-a", "A", "2026-03-31T05:00:00Z")));
        service.push("token-b", List.of(payload("task-b", "B", "2026-03-31T05:30:00Z")));

        List<Task> tokenATasks = service.pull("token-a");
        List<Task> tokenBTasks = service.pull("token-b");

        assertEquals(1, tokenATasks.size());
        assertEquals(1, tokenBTasks.size());
        assertEquals("task-a", tokenATasks.getFirst().getId());
        assertEquals("task-b", tokenBTasks.getFirst().getId());
        assertTrue(service.pull("token-c").isEmpty());
    }

    private SyncTaskPayload payload(String id, String title, String updatedAt) {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-31T02:00:00Z");
        return new SyncTaskPayload(
                id,
                title,
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                null,
                createdAt,
                OffsetDateTime.parse(updatedAt),
                null
        );
    }
}
