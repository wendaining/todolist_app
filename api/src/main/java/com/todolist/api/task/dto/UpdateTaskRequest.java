package com.todolist.api.task.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.todolist.api.task.model.TaskPriority;
import com.todolist.api.task.model.TaskStatus;
import jakarta.validation.constraints.Pattern;

import java.time.OffsetDateTime;

public class UpdateTaskRequest {

    @Pattern(regexp = ".*\\S.*", message = "title must not be blank")
    private String title;

    private TaskStatus status;
    private TaskPriority priority;
    private OffsetDateTime dueAt;
    private boolean dueAtPresent;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public OffsetDateTime getDueAt() {
        return dueAt;
    }

    @JsonSetter("dueAt")
    public void setDueAt(OffsetDateTime dueAt) {
        this.dueAt = dueAt;
        this.dueAtPresent = true;
    }

    @JsonIgnore
    public boolean isDueAtPresent() {
        return dueAtPresent;
    }
}
