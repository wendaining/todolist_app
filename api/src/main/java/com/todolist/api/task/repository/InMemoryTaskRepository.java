package com.todolist.api.task.repository;

import com.todolist.api.task.model.Task;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryTaskRepository implements TaskRepository {

    private final CopyOnWriteArrayList<Task> tasks = new CopyOnWriteArrayList<>();

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks);
    }

    @Override
    public Optional<Task> findById(String id) {
        return tasks.stream().filter(task -> task.getId().equals(id)).findFirst();
    }

    @Override
    public Task save(Task task) {
        tasks.add(task);
        return task;
    }
}
