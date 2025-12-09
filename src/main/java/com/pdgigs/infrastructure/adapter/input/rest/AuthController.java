package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.application.service.AuthenticateUserService;
import com.pdgigs.application.service.RegisterUserService;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.LoginRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.RegisterRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final RegisterUserService registerUserService;
    private final AuthenticateUserService authenticateUserService;

    @Operation(summary = "Register a new user")
    @PostMapping(path = "/register", consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<Void>> register(@Valid @RequestBody RegisterRequest request) {
        return registerUserService.registerUser(request.email(), request.name(), request.password())
                .map(u -> ResponseEntity.status(201).build());
    }

    @Operation(summary = "Authenticate and get JWT token")
    @PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authenticateUserService.authenticate(request.email(), request.password())
                .map(token -> ResponseEntity.ok(new AuthResponse(token)));
    }
}