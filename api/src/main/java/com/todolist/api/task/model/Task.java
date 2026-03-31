package com.todolist.api.task.model;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class Task {

    private String id;
    private String title;
    private TaskStatus status;
    private TaskPriority priority;
    private OffsetDateTime dueAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime completedAt;

    public Task() {
    }

    public Task(
            String id,
            String title,
            TaskStatus status,
            TaskPriority priority,
            OffsetDateTime dueAt,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime completedAt
    ) {
        this.id = id;
        this.title = validateTitle(title);
        this.status = Objects.requireNonNull(status, "status is required");
        this.priority = Objects.requireNonNull(priority, "priority is required");
        this.dueAt = dueAt;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
        this.completedAt = completedAt;
    }

    public static Task createNew(String id, String title, TaskPriority priority, OffsetDateTime dueAt) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return new Task(
                id,
                title,
                TaskStatus.TODO,
                priority == null ? TaskPriority.MEDIUM : priority,
                dueAt,
                now,
                now,
                null
        );
    }

    public void markDone() {
        this.status = TaskStatus.DONE;
        this.completedAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = this.completedAt;
    }

    public void markTodo() {
        this.status = TaskStatus.TODO;
        this.completedAt = null;
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = validateTitle(title);
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = Objects.requireNonNull(status, "status is required");
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = Objects.requireNonNull(priority, "priority is required");
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public OffsetDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(OffsetDateTime dueAt) {
        this.dueAt = dueAt;
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    private String validateTitle(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return raw.trim();
    }
}
