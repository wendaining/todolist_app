package com.todolist.api.task.service;

import com.todolist.api.task.model.Task;
import com.todolist.api.task.model.TaskPriority;
import com.todolist.api.task.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> listTasks() {
        return taskRepository.findAll();
    }

    public Task createTask(String title, TaskPriority priority, OffsetDateTime dueAt) {
        Task task = Task.createNew(UUID.randomUUID().toString(), title, priority, dueAt);
        return taskRepository.save(task);
    }
}
