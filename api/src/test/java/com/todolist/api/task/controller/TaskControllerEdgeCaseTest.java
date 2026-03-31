package com.todolist.api.task.controller;

import com.todolist.api.security.service.TokenSecurityService;
import com.todolist.api.task.model.Task;
import com.todolist.api.task.model.TaskPriority;
import com.todolist.api.task.model.TaskStatus;
import com.todolist.api.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private TokenSecurityService tokenSecurityService;

    @Test
    void createTask_shouldAcceptDueAt() throws Exception {
        OffsetDateTime dueAt = OffsetDateTime.of(2026, 4, 15, 10, 0, 0, 0, ZoneOffset.UTC);
        Task task = Task.createNew("task-due", "task with deadline", TaskPriority.HIGH, dueAt);
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");
        when(taskService.createTask(eq("token-key-1"), eq("task with deadline"), eq(TaskPriority.HIGH), eq(dueAt)))
                .thenReturn(task);

        mockMvc.perform(post("/tasks")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "task with deadline",
                                  "priority": "high",
                                  "dueAt": "2026-04-15T10:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("task-due"))
                .andExpect(jsonPath("$.dueAt").isNotEmpty());
    }

    @Test
    void updateTask_shouldUpdateTitle() throws Exception {
        Task task = Task.createNew("task-title", "original", TaskPriority.MEDIUM, null);
        task.setTitle("updated title");
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");
        when(taskService.updateTask(eq("token-key-1"), eq("task-title"), eq("updated title"), any(), any(), any(), anyBoolean()))
                .thenReturn(Optional.of(task));

        mockMvc.perform(patch("/tasks/task-title")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "updated title"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("updated title"));
    }

    @Test
    void updateTask_shouldRevertDoneToTodo() throws Exception {
        Task task = Task.createNew("task-revert", "revert test", TaskPriority.LOW, null);
        task.markDone();
        task.markTodo();
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");
        when(taskService.updateTask(eq("token-key-1"), eq("task-revert"), any(), eq(TaskStatus.TODO), any(), any(), anyBoolean()))
                .thenReturn(Optional.of(task));

        mockMvc.perform(patch("/tasks/task-revert")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "todo"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("todo"))
                .andExpect(jsonPath("$.completedAt").isEmpty());
    }

    @Test
    void updateTask_shouldClearDueAt() throws Exception {
        Task task = Task.createNew("task-clear-due", "clear dueAt", TaskPriority.MEDIUM, 
                OffsetDateTime.of(2026, 5, 1, 12, 0, 0, 0, ZoneOffset.UTC));
        task.setDueAt(null);
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");
        when(taskService.updateTask(eq("token-key-1"), eq("task-clear-due"), any(), any(), any(), eq(null), eq(true)))
                .thenReturn(Optional.of(task));

        mockMvc.perform(patch("/tasks/task-clear-due")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dueAt": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueAt").isEmpty());
    }

    @Test
    void listTasks_shouldReturn429WhenRateLimited() throws Exception {
        when(tokenSecurityService.authenticateAndConsume("token-1"))
                .thenThrow(new ResponseStatusException(TOO_MANY_REQUESTS, "rate limit exceeded"));

        mockMvc.perform(get("/tasks").header("X-Token", "token-1"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void createTask_shouldRejectMissingToken() throws Exception {
        when(tokenSecurityService.authenticateAndConsume(null))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "X-Token header is required"));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "no token",
                                  "priority": "low"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTask_shouldRejectInvalidPriority() throws Exception {
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");

        mockMvc.perform(post("/tasks")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "bad priority",
                                  "priority": "urgent"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTask_shouldRejectInvalidStatus() throws Exception {
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");

        mockMvc.perform(patch("/tasks/task-1")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "archived"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
