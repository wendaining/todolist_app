package com.todolist.api.task.controller;

import com.todolist.api.security.service.TokenSecurityService;
import com.todolist.api.task.model.Task;
import com.todolist.api.task.model.TaskPriority;
import com.todolist.api.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private TokenSecurityService tokenSecurityService;

    @Test
    void listTasks_shouldReturnTaskArray() throws Exception {
        Task task = Task.createNew("task-1", "read spec", TaskPriority.HIGH, null);
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");
        when(taskService.listTasks("token-key-1")).thenReturn(List.of(task));

        mockMvc.perform(get("/tasks").header("X-Token", "token-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("task-1"))
                .andExpect(jsonPath("$[0].title").value("read spec"))
                .andExpect(jsonPath("$[0].status").value("todo"))
                .andExpect(jsonPath("$[0].priority").value("high"));
    }

    @Test
    void createTask_shouldReturnCreatedTask() throws Exception {
        Task task = Task.createNew("task-2", "write code", TaskPriority.MEDIUM, null);
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");
        when(taskService.createTask(eq("token-key-1"), eq("write code"), eq(TaskPriority.MEDIUM), isNull())).thenReturn(task);

        mockMvc.perform(post("/tasks")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "write code",
                                  "priority": "medium",
                                  "dueAt": null
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("task-2"))
                .andExpect(jsonPath("$.title").value("write code"))
                .andExpect(jsonPath("$.status").value("todo"))
                .andExpect(jsonPath("$.priority").value("medium"));
    }

    @Test
    void createTask_shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");

        mockMvc.perform(post("/tasks")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "priority": "low"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService);
    }

    @Test
    void updateTask_shouldReturnUpdatedTask() throws Exception {
        Task task = Task.createNew("task-3", "write tests", TaskPriority.HIGH, null);
        task.markDone();
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");
        when(taskService.updateTask(eq("token-key-1"), eq("task-3"), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(Optional.of(task));

        mockMvc.perform(patch("/tasks/task-3")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "done"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("task-3"))
                .andExpect(jsonPath("$.status").value("done"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());
    }

    @Test
    void updateTask_shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");
        when(taskService.updateTask(eq("token-key-1"), eq("missing-id"), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/tasks/missing-id")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "done"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTask_shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");

        mockMvc.perform(patch("/tasks/task-3")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService);
    }

    @Test
    void listTasks_shouldReturnUnauthorizedWhenTokenInvalid() throws Exception {
        when(tokenSecurityService.authenticateAndConsume("bad-token"))
                .thenThrow(new ResponseStatusException(UNAUTHORIZED, "invalid token"));

        mockMvc.perform(get("/tasks").header("X-Token", "bad-token"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(taskService);
    }
}
