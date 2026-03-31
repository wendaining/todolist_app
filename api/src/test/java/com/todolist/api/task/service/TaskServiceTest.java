package com.todolist.api.task.service;

import com.todolist.api.task.model.Task;
import com.todolist.api.task.model.TaskPriority;
import com.todolist.api.task.model.TaskStatus;
import com.todolist.api.task.repository.InMemoryTaskRepository;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskServiceTest {

    @Test
    void updateTask_shouldUpdateStatusPriorityAndDueAt() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskService service = new TaskService(repository);
        Task task = Task.createNew("task-1", "learn patch", TaskPriority.LOW, null);
        repository.save(task);
        OffsetDateTime dueAt = OffsetDateTime.parse("2026-04-01T10:15:30+08:00");

        Optional<Task> updated = service.updateTask(
                "task-1",
                null,
                TaskStatus.DONE,
                TaskPriority.HIGH,
                dueAt,
                true
        );

        assertTrue(updated.isPresent());
        assertEquals(TaskStatus.DONE, updated.get().getStatus());
        assertEquals(TaskPriority.HIGH, updated.get().getPriority());
        assertEquals(dueAt, updated.get().getDueAt());
        assertNotNull(updated.get().getCompletedAt());
    }

    @Test
    void updateTask_shouldReturnEmptyWhenTaskNotFound() {
        TaskService service = new TaskService(new InMemoryTaskRepository());

        Optional<Task> updated = service.updateTask(
                "not-exists",
                "new title",
                null,
                null,
                null,
                false
        );

        assertTrue(updated.isEmpty());
    }
}
