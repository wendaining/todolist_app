package com.todolist.api.security.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenSecurityServiceTest {

    @Test
    void authenticate_shouldReturnTokenKeyForValidToken() {
        TokenSecurityService service = new TokenSecurityService("dev-token", 24, 10);

        String tokenKey = service.authenticateAndConsume("dev-token");

        assertNotNull(tokenKey);
    }

    @Test
    void revoke_shouldInvalidateToken() {
        TokenSecurityService service = new TokenSecurityService("dev-token", 24, 10);
        service.revokeToken("dev-token");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.authenticateAndConsume("dev-token")
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void rotate_shouldReturnNewTokenAndInvalidateOldToken() {
        TokenSecurityService service = new TokenSecurityService("dev-token", 24, 100);

        TokenSecurityService.TokenRotateResult result = service.rotateToken("dev-token");
        String newTokenKey = service.authenticateAndConsume(result.token());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.authenticateAndConsume("dev-token")
        );

        assertNotNull(result.token());
        assertNotNull(result.expiresAt());
        assertNotNull(newTokenKey);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void authenticate_shouldRejectWhenRateLimitExceeded() {
        TokenSecurityService service = new TokenSecurityService("dev-token", 24, 1);
        service.authenticateAndConsume("dev-token");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.authenticateAndConsume("dev-token")
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
    }
}
