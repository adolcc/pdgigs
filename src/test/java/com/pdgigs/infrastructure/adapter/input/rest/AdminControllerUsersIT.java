package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.input.*;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.ChangeRoleRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.UserResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.UserRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("AdminController - Users Management Tests")
class AdminControllerUsersIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AdminListUsersUseCase listUsersUseCase;

    @MockitoBean
    private AdminDeleteUserUseCase deleteUserUseCase;

    @MockitoBean
    private AdminChangeUserRoleUseCase changeRoleUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @MockitoBean
    private UserRestMapper userRestMapper;


    @MockitoBean
    private AdminListScoresUseCase listScoresUseCase; // ← FALTABA ESTE


    // También necesitas este si pruebas endpoints de scores
    @MockitoBean
    private ScoreRestMapper scoreRestMapper;

    private User createUser(String id, String email, String name, String role) {
        return new User(id, email, name, "encoded-pass", role, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should list all users and return 200")
    void listAllUsers_AdminUser_Returns200() {
        // Given
        User user1 = createUser("user-1", "user1@test.com", "User One", User.ROLE_USER);
        User user2 = createUser("user-2", "user2@test.com", "User Two", User.ROLE_ADMIN);

        UserResponse response1 = new UserResponse("user-1", "user1@test.com", "User One", User.ROLE_USER);
        UserResponse response2 = new UserResponse("user-2", "user2@test.com", "User Two", User.ROLE_ADMIN);

        when(listUsersUseCase.listAllUsers()).thenReturn(Flux.just(user1, user2));
        when(userRestMapper.toResponse(user1)).thenReturn(response1);
        when(userRestMapper.toResponse(user2)).thenReturn(response2);

        // When & Then
        webTestClient.get()
                .uri("/admin/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("user-1")
                .jsonPath("$[1].id").isEqualTo("user-2");

        verify(listUsersUseCase).listAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get user by ID and return 200")
    void getUserById_AdminUser_Returns200() {
        // Given
        String userId = "507f1f77bcf86cd799439011";
        User user = createUser(userId, "admin@test.com", "Admin User", User.ROLE_ADMIN);
        UserResponse response = new UserResponse(userId, "admin@test.com", "Admin User", User.ROLE_ADMIN);

        when(getUserUseCase.getUserById(eq(userId))).thenReturn(Mono.just(user));
        when(userRestMapper.toResponse(user)).thenReturn(response);

        // When & Then
        webTestClient.get()
                .uri("/admin/users/{id}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(userId)
                .jsonPath("$.email").isEqualTo("admin@test.com");

        verify(getUserUseCase).getUserById(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete user and return 204")
    void deleteUser_AdminUser_Returns204() {
        // Given
        String userId = "507f1f77bcf86cd799439011";
        when(deleteUserUseCase.deleteUser(eq(userId))).thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/admin/users/{id}", userId)
                .exchange()
                .expectStatus().isNoContent();

        verify(deleteUserUseCase).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should change user role and return 200")
    void changeUserRole_AdminUser_Returns200() {
        // Given
        String userId = "507f1f77bcf86cd799439011";
        User updatedUser = createUser(userId, "user@test.com", "Test User", User.ROLE_ADMIN);
        UserResponse response = new UserResponse(userId, "user@test.com", "Test User", User.ROLE_ADMIN);

        when(changeRoleUseCase.changeRole(eq(userId), eq(User.ROLE_ADMIN))).thenReturn(Mono.just(updatedUser));
        when(userRestMapper.toResponse(updatedUser)).thenReturn(response);

        ChangeRoleRequest request = new ChangeRoleRequest(User.ROLE_ADMIN);

        // When & Then
        webTestClient.put()
                .uri("/admin/users/{id}/role", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.role").isEqualTo(User.ROLE_ADMIN);

        verify(changeRoleUseCase).changeRole(userId, User.ROLE_ADMIN);
    }
}