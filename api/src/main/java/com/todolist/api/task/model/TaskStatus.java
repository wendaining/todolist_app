package com.todolist.api.task.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatus {
    TODO("todo"),
    DONE("done");

    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TaskStatus fromValue(String raw) {
        for (TaskStatus status : TaskStatus.values()) {
            if (status.value.equalsIgnoreCase(raw)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported task status: " + raw);
    }
}
