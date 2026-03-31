package com.todolist.api.task.controller;

import com.todolist.api.task.dto.CreateTaskRequest;
import com.todolist.api.task.dto.TaskResponse;
import com.todolist.api.task.dto.UpdateTaskRequest;
import com.todolist.api.task.model.Task;
import com.todolist.api.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public List<TaskResponse> listTasks() {
        return taskService.listTasks().stream().map(TaskResponse::from).toList();
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(request.title(), request.priority(), request.dueAt());
        return TaskResponse.from(task);
    }

    @PatchMapping("/tasks/{id}")
    public TaskResponse updateTask(@PathVariable String id, @Valid @RequestBody UpdateTaskRequest request) {
        Task task = taskService.updateTask(
                        id,
                        request.getTitle(),
                        request.getStatus(),
                        request.getPriority(),
                        request.getDueAt(),
                        request.isDueAtPresent()
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "task not found"));

        return TaskResponse.from(task);
    }
}
