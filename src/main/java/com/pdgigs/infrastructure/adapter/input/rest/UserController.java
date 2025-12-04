package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.infrastructure.adapter.input.rest.dto.request.ChangePasswordRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateEmailRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateNameRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.AuthWithUserResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.UserRestMapper;
import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.input.GetUserUseCase;
import com.pdgigs.domain.port.input.UpdateUserUseCase;
import com.pdgigs.domain.port.output.JwtTokenProvider;
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
@Tag(name = "User", description = "User profile and password management")
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final UserRestMapper userRestMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Update my name and reissue token")
    @PutMapping("/me/name")
    @ResponseStatus(HttpStatus.OK)
    public Mono<AuthWithUserResponse> updateName(@Valid @RequestBody UpdateNameRequest req) {
        return currentUserId()
                .flatMap(id -> updateUserUseCase.updateName(id, req.name()))
                .flatMap(this::buildAuthWithUserResponse);
    }

    @Operation(summary = "Update my email and reissue token")
    @PutMapping("/me/email")
    @ResponseStatus(HttpStatus.OK)
    public Mono<AuthWithUserResponse> updateEmail(@Valid @RequestBody UpdateEmailRequest req) {
        return currentUserId()
                .flatMap(id -> updateUserUseCase.updateEmail(id, req.email()))
                .flatMap(this::buildAuthWithUserResponse);
    }

    @Operation(summary = "Change my password and reissue token")
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.OK)
    public Mono<AuthWithUserResponse> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        return currentUserId()
                .flatMap(id -> updateUserUseCase.changePassword(id, req.currentPassword(), req.newPassword()))
                .flatMap(this::buildAuthWithUserResponse);
    }

    private Mono<AuthWithUserResponse> buildAuthWithUserResponse(User user) {
        return jwtTokenProvider.generateToken(user)
                .map(token -> new AuthWithUserResponse(token, "Bearer", userRestMapper.toResponse(user)));
    }

    private Mono<String> currentUserId() {

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    var auth = ctx.getAuthentication();
                    if (auth == null) {
                        return Mono.empty();
                    }
                    String email = auth.getName();
                    if (email == null || email.isBlank()) {
                        return Mono.empty();
                    }
                    return getUserUseCase.getUserByEmail(email).map(User::id);
                });
    }
}