package com.todolist.api.task.controller;

import com.todolist.api.task.dto.TaskResponse;
import com.todolist.api.task.service.TaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
