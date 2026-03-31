package com.todolist.api.task.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskEdgeCaseTest {

    @Test
    void setTitle_shouldUpdateUpdatedAt() throws InterruptedException {
        Task task = Task.createNew("id-1", "original", TaskPriority.MEDIUM, null);
        OffsetDateTime originalUpdatedAt = task.getUpdatedAt();

        Thread.sleep(10);
        task.setTitle("new title");

        assertTrue(task.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void setPriority_shouldUpdateUpdatedAt() throws InterruptedException {
        Task task = Task.createNew("id-2", "task", TaskPriority.LOW, null);
        OffsetDateTime originalUpdatedAt = task.getUpdatedAt();

        Thread.sleep(10);
        task.setPriority(TaskPriority.HIGH);

        assertTrue(task.getUpdatedAt().isAfter(originalUpdatedAt));
        assertEquals(TaskPriority.HIGH, task.getPriority());
    }

    @Test
    void setDueAt_shouldUpdateUpdatedAt() throws InterruptedException {
        Task task = Task.createNew("id-3", "task", TaskPriority.MEDIUM, null);
        OffsetDateTime originalUpdatedAt = task.getUpdatedAt();

        Thread.sleep(10);
        OffsetDateTime dueAt = OffsetDateTime.of(2026, 5, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        task.setDueAt(dueAt);

        assertTrue(task.getUpdatedAt().isAfter(originalUpdatedAt));
        assertEquals(dueAt, task.getDueAt());
    }

    @Test
    void setDueAt_shouldAllowNull() {
        OffsetDateTime dueAt = OffsetDateTime.of(2026, 5, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        Task task = Task.createNew("id-4", "task", TaskPriority.MEDIUM, dueAt);

        task.setDueAt(null);

        assertNull(task.getDueAt());
    }

    @Test
    void markDone_shouldSetCompletedAt() {
        Task task = Task.createNew("id-5", "task", TaskPriority.MEDIUM, null);

        task.markDone();

        assertEquals(TaskStatus.DONE, task.getStatus());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    void markTodo_shouldClearCompletedAt() {
        Task task = Task.createNew("id-6", "task", TaskPriority.MEDIUM, null);
        task.markDone();

        task.markTodo();

        assertEquals(TaskStatus.TODO, task.getStatus());
        assertNull(task.getCompletedAt());
    }

    @Test
    void markDone_thenMarkTodo_shouldUpdateTimestamps() throws InterruptedException {
        Task task = Task.createNew("id-7", "task", TaskPriority.MEDIUM, null);
        OffsetDateTime beforeDone = task.getUpdatedAt();

        Thread.sleep(10);
        task.markDone();
        OffsetDateTime afterDone = task.getUpdatedAt();

        Thread.sleep(10);
        task.markTodo();
        OffsetDateTime afterTodo = task.getUpdatedAt();

        assertTrue(afterDone.isAfter(beforeDone));
        assertTrue(afterTodo.isAfter(afterDone));
    }

    @Test
    void constructor_shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> 
                new Task("id", "title", null, TaskPriority.MEDIUM, null,
                        OffsetDateTime.now(), OffsetDateTime.now(), null));
    }

    @Test
    void constructor_shouldRejectNullPriority() {
        assertThrows(NullPointerException.class, () -> 
                new Task("id", "title", TaskStatus.TODO, null, null,
                        OffsetDateTime.now(), OffsetDateTime.now(), null));
    }

    @Test
    void constructor_shouldRejectNullCreatedAt() {
        assertThrows(NullPointerException.class, () -> 
                new Task("id", "title", TaskStatus.TODO, TaskPriority.MEDIUM, null,
                        null, OffsetDateTime.now(), null));
    }

    @Test
    void constructor_shouldRejectNullUpdatedAt() {
        assertThrows(NullPointerException.class, () -> 
                new Task("id", "title", TaskStatus.TODO, TaskPriority.MEDIUM, null,
                        OffsetDateTime.now(), null, null));
    }

    @Test
    void createNew_shouldDefaultToMediumPriority() {
        Task task = Task.createNew("id-8", "task", null, null);

        assertEquals(TaskPriority.MEDIUM, task.getPriority());
    }

    @Test
    void setTitle_shouldTrimWhitespace() {
        Task task = Task.createNew("id-9", "original", TaskPriority.MEDIUM, null);

        task.setTitle("  trimmed  ");

        assertEquals("trimmed", task.getTitle());
    }

    @Test
    void constructor_shouldTrimTitle() {
        Task task = Task.createNew("id-10", "  spaced  ", TaskPriority.LOW, null);

        assertEquals("spaced", task.getTitle());
    }

    @Test
    void setTitle_shouldRejectNull() {
        Task task = Task.createNew("id-11", "original", TaskPriority.MEDIUM, null);

        assertThrows(IllegalArgumentException.class, () -> task.setTitle(null));
    }

    @Test
    void setPriority_shouldRejectNull() {
        Task task = Task.createNew("id-12", "task", TaskPriority.MEDIUM, null);

        assertThrows(NullPointerException.class, () -> task.setPriority(null));
    }

    @Test
    void setStatus_shouldRejectNull() {
        Task task = Task.createNew("id-13", "task", TaskPriority.MEDIUM, null);

        assertThrows(NullPointerException.class, () -> task.setStatus(null));
    }
}
