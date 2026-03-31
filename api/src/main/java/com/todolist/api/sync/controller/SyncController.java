package com.todolist.api.sync.controller;

import com.todolist.api.security.service.TokenSecurityService;
import com.todolist.api.sync.dto.SyncPushRequest;
import com.todolist.api.sync.dto.SyncResponse;
import com.todolist.api.sync.service.SyncService;
import com.todolist.api.task.dto.TaskResponse;
import com.todolist.api.task.model.Task;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private final SyncService syncService;
    private final TokenSecurityService tokenSecurityService;

    public SyncController(SyncService syncService, TokenSecurityService tokenSecurityService) {
        this.syncService = syncService;
        this.tokenSecurityService = tokenSecurityService;
    }

    @PostMapping("/pull")
    public SyncResponse pull(
            @RequestHeader(value = "X-Token", required = false) String token,
            @RequestBody(required = false) Object ignored
    ) {
        String tokenKey = tokenSecurityService.authenticateAndConsume(token);
        return buildResponse(syncService.pull(tokenKey));
    }

    @PostMapping("/push")
    public SyncResponse push(
            @RequestHeader(value = "X-Token", required = false) String token,
            @Valid @RequestBody SyncPushRequest request
    ) {
        String tokenKey = tokenSecurityService.authenticateAndConsume(token);
        return buildResponse(syncService.push(tokenKey, request.tasks()));
    }

    private SyncResponse buildResponse(List<Task> tasks) {
        return new SyncResponse(
                tasks.stream().map(TaskResponse::from).toList(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }
}
