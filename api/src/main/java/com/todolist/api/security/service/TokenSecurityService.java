package com.todolist.api.security.service;

import com.todolist.api.security.store.InMemoryTokenStateStore;
import com.todolist.api.security.store.TokenMetadata;
import com.todolist.api.security.store.TokenStateStore;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class TokenSecurityService {

    private final TokenStateStore tokenStateStore;
    private final SecureRandom secureRandom = new SecureRandom();
    private final int tokenTtlHours;
    private final int rateLimitPerMinute;

    @Autowired
    public TokenSecurityService(
            TokenStateStore tokenStateStore,
            @Value("${app.security.bootstrapTokens:dev-token-change-me}") String bootstrapTokens,
            @Value("${app.security.tokenTtlHours:720}") int tokenTtlHours,
            @Value("${app.security.rateLimitPerMinute:120}") int rateLimitPerMinute
    ) {
        this.tokenStateStore = tokenStateStore;
        this.tokenTtlHours = tokenTtlHours;
        this.rateLimitPerMinute = rateLimitPerMinute;
        initBootstrapTokens(bootstrapTokens);
    }

    public TokenSecurityService(String bootstrapTokens, int tokenTtlHours, int rateLimitPerMinute) {
        this(new InMemoryTokenStateStore(), bootstrapTokens, tokenTtlHours, rateLimitPerMinute);
    }

    public String authenticateAndConsume(String rawToken) {
        String normalized = normalizeToken(rawToken);
        String hash = hashToken(normalized);
        TokenMetadata record = tokenStateStore.findTokenByHash(hash);

        if (record == null || record.revokedAt() != null || record.expiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
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
        tokenStateStore.revokeToken(currentHash, now);

        String newToken = generateRawToken();
        TokenMetadata newRecord = registerToken(newToken, now);
        return new TokenRotateResult(newToken, newRecord.expiresAt());
    }

    public void revokeToken(String rawToken) {
        String currentHash = authenticateAndConsume(rawToken);
        tokenStateStore.revokeToken(currentHash, OffsetDateTime.now(ZoneOffset.UTC));
    }

    private boolean consumeRateLimit(String tokenHash) {
        long currentMinute = OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond() / 60;
        int count = tokenStateStore.incrementAndGetRateCount(tokenHash, currentMinute);
        return count <= rateLimitPerMinute;
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

    private TokenMetadata registerToken(String rawToken, OffsetDateTime now) {
        String hash = hashToken(rawToken);
        TokenMetadata tokenMetadata = new TokenMetadata(
                hash,
                now,
                now.plusHours(tokenTtlHours),
                null
        );
        tokenStateStore.insertTokenIfAbsent(tokenMetadata);
        return tokenMetadata;
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
}
