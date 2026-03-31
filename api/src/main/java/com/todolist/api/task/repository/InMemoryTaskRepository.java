package com.todolist.api.task.repository;

import com.todolist.api.task.model.Task;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation for testing purposes only.
 * Production uses JdbcTaskRepository.
 */
public class InMemoryTaskRepository implements TaskRepository {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Task>> tokenSpaces = new ConcurrentHashMap<>();

    @Override
    public List<Task> findAll(String tokenKey) {
        Map<String, Task> space = tokenSpaces.computeIfAbsent(tokenKey, key -> new ConcurrentHashMap<>());
        List<Task> tasks = new ArrayList<>(space.values());
        tasks.sort(Comparator
                .comparing(Task::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Task::getId));
        return tasks;
    }

    @Override
    public Optional<Task> findById(String tokenKey, String id) {
        Map<String, Task> space = tokenSpaces.computeIfAbsent(tokenKey, key -> new ConcurrentHashMap<>());
        return Optional.ofNullable(space.get(id));
    }

    @Override
    public Task save(String tokenKey, Task task) {
        ConcurrentHashMap<String, Task> space = tokenSpaces.computeIfAbsent(tokenKey, key -> new ConcurrentHashMap<>());
        space.put(task.getId(), task);
        return task;
    }
}
