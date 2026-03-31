package com.todolist.api.security.controller;

import com.todolist.api.security.service.TokenSecurityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenSecurityService tokenSecurityService;

    @Test
    void rotate_shouldReturnNewToken() throws Exception {
        when(tokenSecurityService.rotateToken("token-old"))
                .thenReturn(new TokenSecurityService.TokenRotateResult(
                        "token-new",
                        OffsetDateTime.parse("2026-04-30T00:00:00Z")
                ));

        mockMvc.perform(post("/auth/token/rotate")
                        .header("X-Token", "token-old")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-new"))
                .andExpect(jsonPath("$.expiresAt").value("2026-04-30T00:00:00Z"));
    }

    @Test
    void revoke_shouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/auth/token/revoke")
                        .header("X-Token", "token-old")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
