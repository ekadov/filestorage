package com.eugene.filestorage.controller;

import com.eugene.filestorage.api.dto.AuthTokenRequest;
import com.eugene.filestorage.api.dto.AuthTokenResponse;
import com.eugene.filestorage.security.JwtService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final JwtService jwtService;

    @PostMapping("/token")
    public Mono<AuthTokenResponse> token(@Valid @RequestBody AuthTokenRequest request) {
        String token = jwtService.issueToken(request.getUsername(), request.getRole());
        return Mono.just(new AuthTokenResponse().setToken(token));
    }
}
