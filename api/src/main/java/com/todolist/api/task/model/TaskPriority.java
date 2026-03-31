package com.todolist.api.task.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskPriority {
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low");

    private final String value;

    TaskPriority(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TaskPriority fromValue(String raw) {
        for (TaskPriority priority : TaskPriority.values()) {
            if (priority.value.equalsIgnoreCase(raw)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unsupported task priority: " + raw);
    }
}
