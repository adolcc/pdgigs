package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.AdminChangeUserRoleUseCase;
import com.pdgigs.domain.port.input.AdminDeleteScoreUseCase;
import com.pdgigs.domain.port.input.AdminDeleteUserUseCase;
import com.pdgigs.domain.port.input.AdminListScoresUseCase;
import com.pdgigs.domain.port.input.AdminListUsersUseCase;
import com.pdgigs.domain.port.input.GetUserUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.ChangeRoleRequest;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.UserRestMapper;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ScoreResponse;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative endpoints")
public class AdminController {

    private final AdminListScoresUseCase listScoresUseCase;
    private final AdminDeleteScoreUseCase deleteScoreUseCase;
    private final AdminDeleteUserUseCase deleteUserUseCase;
    private final AdminChangeUserRoleUseCase changeRoleUseCase;
    private final AdminListUsersUseCase listUsersUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ScoreRestMapper scoreRestMapper;
    private final UserRestMapper userRestMapper;

    @Operation(summary = "List all scores (admin only)")
    @GetMapping("/scores")
    public Flux<ScoreResponse> listAllScores() {
        return listScoresUseCase.listAllScores().map(scoreRestMapper::toResponse);
    }

    @Operation(summary = "Delete a score by id (admin only)")
    @DeleteMapping("/scores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteScore(@PathVariable("id") String id) {
        return deleteScoreUseCase.deleteScore(id);
    }

    @Operation(summary = "List all users (admin only)")
    @GetMapping("/users")
    public Flux<UserResponse> listAllUsers() {
        return listUsersUseCase.listAllUsers().map(userRestMapper::toResponse);
    }

    @Operation(summary = "Get a user by id (admin only)")
    @GetMapping("/users/{id}")
    public Mono<UserResponse> getUserById(@PathVariable("id") String id) {
        return getUserUseCase.getUserById(id).map(userRestMapper::toResponse);
    }

    @Operation(summary = "Delete a user by id (admin only)")
    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteUser(@PathVariable("id") String id) {
        return deleteUserUseCase.deleteUser(id);
    }

    @Operation(summary = "Change role of a user (admin only)")
    @PutMapping("/users/{id}/role")
    public Mono<UserResponse> changeUserRole(@PathVariable("id") String id, @Valid @RequestBody ChangeRoleRequest req) {
        return changeRoleUseCase.changeRole(id, req.role())
                .map(userRestMapper::toResponse);
    }
}