package com.todolist.api.security.store;

import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTokenStateStore implements TokenStateStore {

    private final ConcurrentHashMap<String, TokenMetadata> tokenStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentHashMap<Long, Integer>> rateWindows = new ConcurrentHashMap<>();

    @Override
    public TokenMetadata findTokenByHash(String tokenHash) {
        return tokenStore.get(tokenHash);
    }

    @Override
    public void insertTokenIfAbsent(TokenMetadata tokenMetadata) {
        tokenStore.putIfAbsent(tokenMetadata.tokenHash(), tokenMetadata);
    }

    @Override
    public void insertOrReplaceToken(TokenMetadata tokenMetadata) {
        tokenStore.put(tokenMetadata.tokenHash(), tokenMetadata);
    }

    @Override
    public void revokeToken(String tokenHash, OffsetDateTime revokedAt) {
        tokenStore.computeIfPresent(tokenHash, (key, current) ->
                new TokenMetadata(current.tokenHash(), current.createdAt(), current.expiresAt(), revokedAt)
        );
    }

    @Override
    public int incrementAndGetRateCount(String tokenHash, long windowMinute) {
        ConcurrentHashMap<Long, Integer> windows = rateWindows.computeIfAbsent(tokenHash, key -> new ConcurrentHashMap<>());
        return windows.merge(windowMinute, 1, Integer::sum);
    }
}
