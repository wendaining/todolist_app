package com.todolist.api.security.dto;

import java.time.OffsetDateTime;

public record TokenRotateResponse(
        String token,
        OffsetDateTime expiresAt
) {
}
