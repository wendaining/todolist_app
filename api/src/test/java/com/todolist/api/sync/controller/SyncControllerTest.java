package com.todolist.api.sync.controller;

import com.todolist.api.security.service.TokenSecurityService;
import com.todolist.api.sync.service.SyncService;
import com.todolist.api.task.model.Task;
import com.todolist.api.task.model.TaskPriority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SyncController.class)
class SyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SyncService syncService;

    @MockBean
    private TokenSecurityService tokenSecurityService;

    @Test
    void pull_shouldReturnTokenScopedTasks() throws Exception {
        Task task = Task.createNew("sync-1", "from cloud", TaskPriority.MEDIUM, null);
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");
        when(syncService.pull("token-key-1")).thenReturn(List.of(task));

        mockMvc.perform(post("/sync/pull")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[0].id").value("sync-1"))
                .andExpect(jsonPath("$.tasks[0].title").value("from cloud"))
                .andExpect(jsonPath("$.serverTime").isNotEmpty());
    }

    @Test
    void push_shouldMergeAndReturnTasks() throws Exception {
        Task mergedTask = Task.createNew("sync-2", "merged", TaskPriority.HIGH, null);
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");
        when(syncService.push(eq("token-key-1"), anyList())).thenReturn(List.of(mergedTask));

        mockMvc.perform(post("/sync/push")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tasks": [
                                    {
                                      "id": "sync-2",
                                      "title": "merged",
                                      "status": "todo",
                                      "priority": "high",
                                      "dueAt": null,
                                      "createdAt": "2026-03-31T03:00:00Z",
                                      "updatedAt": "2026-03-31T03:30:00Z",
                                      "completedAt": null
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[0].id").value("sync-2"))
                .andExpect(jsonPath("$.tasks[0].priority").value("high"))
                .andExpect(jsonPath("$.serverTime").isNotEmpty());
    }

    @Test
    void push_shouldReturnBadRequestWhenTokenBlank() throws Exception {
        when(tokenSecurityService.authenticateAndConsume("   "))
                .thenThrow(new ResponseStatusException(UNAUTHORIZED, "X-Token header is required"));

        mockMvc.perform(post("/sync/push")
                        .header("X-Token", "   ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + "\"tasks\": []}"))
          .andExpect(status().isUnauthorized());

        verifyNoInteractions(syncService);
    }

    @Test
    void push_shouldReturnBadRequestWhenTasksMissing() throws Exception {
        when(tokenSecurityService.authenticateAndConsume("token-1")).thenReturn("token-key-1");

        mockMvc.perform(post("/sync/push")
                        .header("X-Token", "token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(syncService);
    }

    @Test
    void pull_shouldReturnUnauthorizedWhenTokenInvalid() throws Exception {
        when(tokenSecurityService.authenticateAndConsume("bad-token"))
                .thenThrow(new ResponseStatusException(UNAUTHORIZED, "invalid token"));

        mockMvc.perform(post("/sync/pull")
                        .header("X-Token", "bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(syncService);
    }
}
