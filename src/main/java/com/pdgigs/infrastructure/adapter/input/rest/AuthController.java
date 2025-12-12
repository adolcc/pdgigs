package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.application.service.AuthenticateUserService;
import com.pdgigs.application.service.RegisterUserService;
import com.pdgigs.domain.port.output.JwtTokenProvider;
import com.pdgigs.domain.port.output.UserRepository;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.LoginRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.RegisterRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping(path = "/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final RegisterUserService registerUserService;
    private final AuthenticateUserService authenticateUserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

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
                .flatMap(token ->
                        jwtTokenProvider.extractEmail(token).defaultIfEmpty("")
                                .flatMap(emailFromToken ->
                                        jwtTokenProvider.extractRole(token).defaultIfEmpty("")
                                                .flatMap(roleFromToken -> {
                                                    log.debug("Login token generated. emailFromToken={}, roleFromToken={}", emailFromToken, roleFromToken);

                                                    if (emailFromToken == null || emailFromToken.isBlank()) {
                                                        log.warn("Token does not contain email claim, returning token only");
                                                        return Mono.just(ResponseEntity.ok(new AuthResponse(token)));
                                                    }

                                                    return userRepository.findByEmail(emailFromToken)
                                                            .map(user -> {
                                                                log.info("User found in DB for email {}: role={}", emailFromToken, user.role());
                                                                return ResponseEntity.ok(AuthResponse.fromTokenAndUser(token, user));
                                                            })
                                                            .defaultIfEmpty(ResponseEntity.ok(new AuthResponse(token, emailFromToken, null, roleFromToken)));
                                                })
                                )
                )
                .doOnError(e -> log.warn("Authentication flow failed: {}", e.getMessage()));
    }
}