package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.application.service.AdminService;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.model.User;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.ChangeRoleRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "List all scores (admin only)")
    @GetMapping(path = "/scores", produces = "application/json")
    public Flux<Score> listAllScores() {
        return adminService.listAllScores();
    }

    @Operation(summary = "List all users (admin only)")
    @GetMapping(path = "/users", produces = "application/json")
    public Flux<User> listAllUsers() {
        return adminService.listAllUsers();
    }

    @Operation(summary = "Delete a user (admin only)")
    @DeleteMapping(path = "/users/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable("id") String id) {
        return adminService.deleteUser(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Change role of a user (admin only)")
    @PostMapping(path = "/users/{id}/role", consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<User>> changeUserRole(@PathVariable("id") String id,
                                                     @RequestBody ChangeRoleRequest request) {
        return adminService.changeRole(id, request.role())
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Delete a score (admin only)")
    @DeleteMapping(path = "/scores/{id}")
    public Mono<ResponseEntity<Void>> deleteScore(@PathVariable("id") String id) {
        return adminService.deleteScore(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}