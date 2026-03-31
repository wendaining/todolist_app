package com.todolist.api.sync.service;

import com.todolist.api.sync.dto.SyncTaskPayload;
import com.todolist.api.task.model.Task;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SyncService {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Task>> tokenSpaces = new ConcurrentHashMap<>();

    public List<Task> pull(String token) {
        Map<String, Task> space = tokenSpaces.computeIfAbsent(token, key -> new ConcurrentHashMap<>());
        return snapshot(space);
    }

    public List<Task> push(String token, List<SyncTaskPayload> incomingTasks) {
        ConcurrentHashMap<String, Task> space = tokenSpaces.computeIfAbsent(token, key -> new ConcurrentHashMap<>());

        for (SyncTaskPayload payload : incomingTasks) {
            Task incoming = copy(payload.toTask());
            space.merge(
                    incoming.getId(),
                    incoming,
                    (current, candidate) -> shouldReplace(current, candidate) ? candidate : current
            );
        }

        return snapshot(space);
    }

    private boolean shouldReplace(Task current, Task candidate) {
        OffsetDateTime currentUpdatedAt = current.getUpdatedAt();
        OffsetDateTime candidateUpdatedAt = candidate.getUpdatedAt();

        if (currentUpdatedAt == null) {
            return true;
        }
        if (candidateUpdatedAt == null) {
            return false;
        }
        return !candidateUpdatedAt.isBefore(currentUpdatedAt);
    }

    private List<Task> snapshot(Map<String, Task> space) {
        List<Task> result = new ArrayList<>();
        for (Task task : space.values()) {
            result.add(copy(task));
        }
        result.sort(Comparator
                .comparing(Task::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Task::getId));
        return result;
    }

    private Task copy(Task task) {
        return new Task(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                task.getDueAt(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getCompletedAt()
        );
    }
}
