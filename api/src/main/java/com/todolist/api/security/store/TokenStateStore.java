package com.todolist.api.security.store;

import java.time.OffsetDateTime;

public interface TokenStateStore {

    TokenMetadata findTokenByHash(String tokenHash);

    void insertTokenIfAbsent(TokenMetadata tokenMetadata);

    void insertOrReplaceToken(TokenMetadata tokenMetadata);

    void revokeToken(String tokenHash, OffsetDateTime revokedAt);

    int incrementAndGetRateCount(String tokenHash, long windowMinute);
}
