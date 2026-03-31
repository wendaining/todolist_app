package com.todolist.api.sync.service;

import com.todolist.api.sync.dto.SyncTaskPayload;
import com.todolist.api.task.model.Task;
import com.todolist.api.task.model.TaskPriority;
import com.todolist.api.task.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyncServiceEdgeCaseTest {

    @Test
    void push_shouldRejectOlderUpdateWhenNewerExists() {
        SyncService service = new SyncService();

        // First push with newer timestamp
        service.push("token-a", List.of(payload("task-1", "newer", "2026-03-31T05:00:00Z")));
        // Then push with older timestamp - should be ignored
        service.push("token-a", List.of(payload("task-1", "older", "2026-03-31T03:00:00Z")));

        List<Task> tasks = service.pull("token-a");
        assertEquals(1, tasks.size());
        assertEquals("newer", tasks.getFirst().getTitle());
        assertEquals(OffsetDateTime.parse("2026-03-31T05:00:00Z"), tasks.getFirst().getUpdatedAt());
    }

    @Test
    void push_shouldMergeMultipleTasksInSinglePush() {
        SyncService service = new SyncService();

        service.push("token-a", List.of(
                payload("task-1", "first", "2026-03-31T04:00:00Z"),
                payload("task-2", "second", "2026-03-31T04:30:00Z"),
                payload("task-3", "third", "2026-03-31T05:00:00Z")
        ));

        List<Task> tasks = service.pull("token-a");
        assertEquals(3, tasks.size());
    }

    @Test
    void push_shouldPreserveCompletedStatus() {
        SyncService service = new SyncService();
        OffsetDateTime completedAt = OffsetDateTime.parse("2026-03-31T06:00:00Z");

        SyncTaskPayload doneTask = new SyncTaskPayload(
                "task-done",
                "completed task",
                TaskStatus.DONE,
                TaskPriority.HIGH,
                null,
                OffsetDateTime.parse("2026-03-31T02:00:00Z"),
                OffsetDateTime.parse("2026-03-31T06:00:00Z"),
                completedAt
        );
        service.push("token-a", List.of(doneTask));

        List<Task> tasks = service.pull("token-a");
        assertEquals(1, tasks.size());
        assertEquals(TaskStatus.DONE, tasks.getFirst().getStatus());
        assertNotNull(tasks.getFirst().getCompletedAt());
    }

    @Test
    void push_shouldPreserveDueAt() {
        SyncService service = new SyncService();
        OffsetDateTime dueAt = OffsetDateTime.parse("2026-04-15T10:00:00Z");

        SyncTaskPayload taskWithDue = new SyncTaskPayload(
                "task-due",
                "task with deadline",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                dueAt,
                OffsetDateTime.parse("2026-03-31T02:00:00Z"),
                OffsetDateTime.parse("2026-03-31T03:00:00Z"),
                null
        );
        service.push("token-a", List.of(taskWithDue));

        List<Task> tasks = service.pull("token-a");
        assertEquals(1, tasks.size());
        assertEquals(dueAt, tasks.getFirst().getDueAt());
    }

    @Test
    void push_shouldHandleEmptyTasksList() {
        SyncService service = new SyncService();

        // Pre-populate with a task
        service.push("token-a", List.of(payload("task-1", "existing", "2026-03-31T04:00:00Z")));
        // Push empty list - should not delete existing
        service.push("token-a", List.of());

        List<Task> tasks = service.pull("token-a");
        assertEquals(1, tasks.size());
    }

    @Test
    void pull_shouldReturnEmptyForNewToken() {
        SyncService service = new SyncService();

        List<Task> tasks = service.pull("brand-new-token");
        assertTrue(tasks.isEmpty());
    }

    @Test
    void push_shouldAllowSameTimestampUpdate() {
        SyncService service = new SyncService();
        String sameTime = "2026-03-31T04:00:00Z";

        service.push("token-a", List.of(payload("task-1", "first version", sameTime)));
        service.push("token-a", List.of(payload("task-1", "second version", sameTime)));

        List<Task> tasks = service.pull("token-a");
        assertEquals(1, tasks.size());
        // With same timestamp, later push should win (last-writer-wins)
        assertEquals("second version", tasks.getFirst().getTitle());
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
