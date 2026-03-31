package com.todolist.api.task.service;

import com.todolist.api.task.model.Task;
import com.todolist.api.task.model.TaskPriority;
import com.todolist.api.task.model.TaskStatus;
import com.todolist.api.task.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> listTasks(String tokenKey) {
        return taskRepository.findAll(tokenKey);
    }

    public Task createTask(String tokenKey, String title, TaskPriority priority, OffsetDateTime dueAt) {
        Task task = Task.createNew(UUID.randomUUID().toString(), title, priority, dueAt);
        return taskRepository.save(tokenKey, task);
    }

    public Optional<Task> updateTask(
            String tokenKey,
            String id,
            String title,
            TaskStatus status,
            TaskPriority priority,
            OffsetDateTime dueAt,
            boolean dueAtPresent
    ) {
        Optional<Task> taskOptional = taskRepository.findById(tokenKey, id);
        if (taskOptional.isEmpty()) {
            return Optional.empty();
        }

        Task task = taskOptional.get();

        if (title != null) {
            task.setTitle(title);
        }

        if (status != null && status != task.getStatus()) {
            if (status == TaskStatus.DONE) {
                task.markDone();
            } else {
                task.markTodo();
            }
        }

        if (priority != null && priority != task.getPriority()) {
            task.setPriority(priority);
        }

        if (dueAtPresent) {
            task.setDueAt(dueAt);
        }

        return Optional.of(taskRepository.save(tokenKey, task));
    }
}
