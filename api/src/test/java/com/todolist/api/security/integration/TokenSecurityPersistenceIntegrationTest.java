package com.todolist.api.security.integration;

import com.todolist.api.security.service.TokenSecurityService;
import com.todolist.api.security.store.JdbcTokenStateStore;
import com.todolist.api.security.store.TokenStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TokenSecurityPersistenceIntegrationTest {

    private static final String SQLITE_TEST_URL = createSqliteTestUrl();

    @Autowired
    private TokenStateStore tokenStateStore;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> SQLITE_TEST_URL);
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("app.security.bootstrapTokens", () -> "");
    }

    @BeforeEach
    void cleanup() {
        jdbcTemplate.update("DELETE FROM security_rate_windows");
        jdbcTemplate.update("DELETE FROM security_tokens");
    }

    @Test
    void tokenMetadata_shouldRemainAvailableAfterServiceRecreation() {
        TokenSecurityService firstService = new TokenSecurityService(tokenStateStore, "offline-token", 24, 10);
        String firstTokenHash = firstService.authenticateAndConsume("offline-token");

        TokenSecurityService recreatedService = new TokenSecurityService(tokenStateStore, "", 24, 10);
        String recreatedTokenHash = recreatedService.authenticateAndConsume("offline-token");

        Integer tokenCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM security_tokens", Integer.class);

        assertEquals(firstTokenHash, recreatedTokenHash);
        assertEquals(1, tokenCount);
    }

    @Test
    void rateWindowCounter_shouldRemainAvailableAfterStoreRecreation() {
        TokenSecurityService service = new TokenSecurityService(tokenStateStore, "rate-token", 24, 10);
        String tokenHash = service.authenticateAndConsume("rate-token");
        long windowMinute = 987654L;

        int firstCount = tokenStateStore.incrementAndGetRateCount(tokenHash, windowMinute);

        JdbcTokenStateStore recreatedStore = new JdbcTokenStateStore(jdbcTemplate);
        int secondCount = recreatedStore.incrementAndGetRateCount(tokenHash, windowMinute);

        Integer persistedCount = jdbcTemplate.queryForObject(
                "SELECT request_count FROM security_rate_windows WHERE token_hash = ? AND window_minute = ?",
                Integer.class,
                tokenHash,
                windowMinute
        );

        assertEquals(1, firstCount);
        assertEquals(2, secondCount);
        assertEquals(2, persistedCount);
    }

    private static String createSqliteTestUrl() {
        try {
            Path tempDb = Files.createTempFile("todo-security-it-", ".db");
            return "jdbc:sqlite:" + tempDb.toAbsolutePath();
        } catch (IOException ex) {
            throw new IllegalStateException("failed to create sqlite test db", ex);
        }
    }
}