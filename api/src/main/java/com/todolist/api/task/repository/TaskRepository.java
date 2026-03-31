package com.todolist.api.task.repository;

import com.todolist.api.task.model.Task;

import java.util.List;

public interface TaskRepository {

    List<Task> findAll();

    Task save(Task task);
}
