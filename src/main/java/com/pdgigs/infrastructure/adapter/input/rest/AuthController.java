package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.AuthenticateUserUseCase;
import com.pdgigs.domain.port.input.RegisterUserUseCase;
import com.pdgigs.domain.port.output.UserRepository;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.LoginRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.RegisterRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.AuthResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.UserRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final UserRepository userRepository;
    private final UserRestMapper userRestMapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for email: {}", request.email());

        return registerUserUseCase.registerUser(request.email(), request.name(), request.password())
                .flatMap(user -> authenticateUserUseCase.authenticate(request.email(), request.password())
                        .map(token -> new AuthResponse(token, user.email(), user.name(), user.role()))
                );
    }

    @PostMapping("/login")
    public Mono<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.email());

        return authenticateUserUseCase.authenticate(request.email(), request.password())
                .flatMap(token ->
                        userRepository.findByEmail(request.email())
                                .map(user -> new AuthResponse(token, user.email(), user.name(), user.role()))
                                .switchIfEmpty(Mono.just(new AuthResponse(token, request.email(), null, null)))
                );
    }
}