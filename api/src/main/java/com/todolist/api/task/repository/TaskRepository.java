package com.todolist.api.task.repository;

import com.todolist.api.task.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    List<Task> findAll();

    Optional<Task> findById(String id);

    Task save(Task task);
}
