package com.todolist.api.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenSecurityService {

    private final ConcurrentHashMap<String, TokenRecord> tokenStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateWindow> rateWindows = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final int tokenTtlHours;
    private final int rateLimitPerMinute;

    public TokenSecurityService(
            @Value("${app.security.bootstrapTokens:dev-token-change-me}") String bootstrapTokens,
            @Value("${app.security.tokenTtlHours:720}") int tokenTtlHours,
            @Value("${app.security.rateLimitPerMinute:120}") int rateLimitPerMinute
    ) {
        this.tokenTtlHours = tokenTtlHours;
        this.rateLimitPerMinute = rateLimitPerMinute;
        initBootstrapTokens(bootstrapTokens);
    }

    public String authenticateAndConsume(String rawToken) {
        String normalized = normalizeToken(rawToken);
        String hash = hashToken(normalized);
        TokenRecord record = tokenStore.get(hash);

        if (record == null || record.revokedAt != null || record.expiresAt.isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token");
        }

        if (!consumeRateLimit(hash)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "rate limit exceeded");
        }

        return hash;
    }

    public TokenRotateResult rotateToken(String rawToken) {
        String currentHash = authenticateAndConsume(rawToken);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        TokenRecord current = tokenStore.get(currentHash);
        current.revokedAt = now;

        String newToken = generateRawToken();
        registerToken(newToken, now);
        TokenRecord newRecord = tokenStore.get(hashToken(newToken));
        return new TokenRotateResult(newToken, newRecord.expiresAt);
    }

    public void revokeToken(String rawToken) {
        String currentHash = authenticateAndConsume(rawToken);
        TokenRecord current = tokenStore.get(currentHash);
        current.revokedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    private boolean consumeRateLimit(String tokenHash) {
        long currentMinute = OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond() / 60;
        RateWindow window = rateWindows.compute(tokenHash, (key, existing) -> {
            if (existing == null || existing.windowMinute != currentMinute) {
                return new RateWindow(currentMinute, 1);
            }
            existing.count += 1;
            return existing;
        });
        return window.count <= rateLimitPerMinute;
    }

    private void initBootstrapTokens(String bootstrapTokens) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        for (String token : bootstrapTokens.split(",")) {
            String normalized = token.trim();
            if (!normalized.isEmpty()) {
                registerToken(normalized, now);
            }
        }
    }

    private void registerToken(String rawToken, OffsetDateTime now) {
        String hash = hashToken(rawToken);
        tokenStore.put(hash, new TokenRecord(
                UUID.randomUUID().toString(),
                hash,
                now,
                now.plusHours(tokenTtlHours),
                null
        ));
    }

    private String normalizeToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-Token header is required");
        }
        return rawToken.trim();
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String generateRawToken() {
        byte[] random = new byte[32];
        secureRandom.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    public record TokenRotateResult(String token, OffsetDateTime expiresAt) {
    }

    private static class TokenRecord {
        private final String id;
        private final String tokenHash;
        private final OffsetDateTime createdAt;
        private final OffsetDateTime expiresAt;
        private OffsetDateTime revokedAt;

        private TokenRecord(
                String id,
                String tokenHash,
                OffsetDateTime createdAt,
                OffsetDateTime expiresAt,
                OffsetDateTime revokedAt
        ) {
            this.id = Objects.requireNonNull(id, "id is required");
            this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash is required");
            this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
            this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt is required");
            this.revokedAt = revokedAt;
        }
    }

    private static class RateWindow {
        private final long windowMinute;
        private int count;

        private RateWindow(long windowMinute, int count) {
            this.windowMinute = windowMinute;
            this.count = count;
        }
    }
}
