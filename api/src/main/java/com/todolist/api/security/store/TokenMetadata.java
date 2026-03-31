package com.todolist.api.security.store;

import java.time.OffsetDateTime;

public record TokenMetadata(
        String tokenHash,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        OffsetDateTime revokedAt
) {
}
