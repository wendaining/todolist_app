package com.todolist.api.task.repository;

import com.todolist.api.task.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    List<Task> findAll(String tokenKey);

    Optional<Task> findById(String tokenKey, String id);

    Task save(String tokenKey, Task task);
}
