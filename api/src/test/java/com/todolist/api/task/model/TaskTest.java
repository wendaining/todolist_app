package com.todolist.api.task.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskTest {

    @Test
    void createNew_shouldUseDefaults() {
        Task task = Task.createNew("id-1", "learn testing", null, null);

        assertEquals("id-1", task.getId());
        assertEquals("learn testing", task.getTitle());
        assertEquals(TaskStatus.TODO, task.getStatus());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertNull(task.getCompletedAt());
    }

    @Test
    void markDone_andMarkTodo_shouldSwitchStatus() {
        Task task = Task.createNew("id-2", "write tests", TaskPriority.HIGH, OffsetDateTime.now());

        task.markDone();
        assertEquals(TaskStatus.DONE, task.getStatus());

        task.markTodo();
        assertEquals(TaskStatus.TODO, task.getStatus());
        assertNull(task.getCompletedAt());
    }

    @Test
    void setTitle_shouldRejectBlankValue() {
        Task task = Task.createNew("id-3", "valid title", TaskPriority.LOW, null);

        assertThrows(IllegalArgumentException.class, () -> task.setTitle("   "));
    }
}
