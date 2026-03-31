package com.todolist.api.sync.controller;

import com.todolist.api.sync.dto.SyncPushRequest;
import com.todolist.api.sync.dto.SyncResponse;
import com.todolist.api.sync.service.SyncService;
import com.todolist.api.task.dto.TaskResponse;
import com.todolist.api.task.model.Task;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/pull")
    public SyncResponse pull(@RequestHeader("X-Token") String token, @RequestBody(required = false) Object ignored) {
        String normalizedToken = requireToken(token);
        return buildResponse(syncService.pull(normalizedToken));
    }

    @PostMapping("/push")
    public SyncResponse push(@RequestHeader("X-Token") String token, @Valid @RequestBody SyncPushRequest request) {
        String normalizedToken = requireToken(token);
        return buildResponse(syncService.push(normalizedToken, request.tasks()));
    }

    private String requireToken(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Token header is required");
        }
        return token.trim();
    }

    private SyncResponse buildResponse(List<Task> tasks) {
        return new SyncResponse(
                tasks.stream().map(TaskResponse::from).toList(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }
}
