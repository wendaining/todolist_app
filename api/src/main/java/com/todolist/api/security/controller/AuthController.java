package com.todolist.api.security.controller;

import com.todolist.api.security.dto.TokenRotateResponse;
import com.todolist.api.security.service.TokenSecurityService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/token")
public class AuthController {

    private final TokenSecurityService tokenSecurityService;

    public AuthController(TokenSecurityService tokenSecurityService) {
        this.tokenSecurityService = tokenSecurityService;
    }

    @PostMapping("/rotate")
    public TokenRotateResponse rotate(@RequestHeader(value = "X-Token", required = false) String token) {
        TokenSecurityService.TokenRotateResult result = tokenSecurityService.rotateToken(token);
        return new TokenRotateResponse(result.token(), result.expiresAt());
    }

    @PostMapping("/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@RequestHeader(value = "X-Token", required = false) String token) {
        tokenSecurityService.revokeToken(token);
    }
}
