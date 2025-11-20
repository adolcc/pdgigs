package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.infrastructure.adapter.input.rest.dto.request.ChangePasswordRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateEmailRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateNameRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.UserResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.UserRestMapper;
import com.pdgigs.domain.port.input.GetUserUseCase;
import com.pdgigs.domain.port.input.UpdateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Update", description = "User profile and password management")
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final UserRestMapper userRestMapper;

    @Operation(summary = "Update my name")
    @PutMapping("/me/name")
    @ResponseStatus(HttpStatus.OK)
    public Mono<UserResponse> updateName(@Valid @RequestBody UpdateNameRequest req) {
        return currentUserId()
                .flatMap(id -> updateUserUseCase.updateName(id, req.name()))
                .map(userRestMapper::toResponse);
    }

    @Operation(summary = "Update my email")
    @PutMapping("/me/email")
    @ResponseStatus(HttpStatus.OK)
    public Mono<UserResponse> updateEmail(@Valid @RequestBody UpdateEmailRequest req) {
        return currentUserId()
                .flatMap(id -> updateUserUseCase.updateEmail(id, req.email()))
                .map(userRestMapper::toResponse);
    }

    @Operation(summary = "Change my password")
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        return currentUserId()
                .flatMap(id -> updateUserUseCase.changePassword(id, req.currentPassword(), req.newPassword()))
                .then();
    }

    private Mono<String> currentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .flatMap(auth -> {
                    String email = (auth != null && auth.getPrincipal() instanceof String) ? (String) auth.getPrincipal() : null;
                    return email == null ? Mono.empty() : getUserUseCase.getUserByEmail(email).map(u -> u.id());
                });
    }
}