package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.input.GetUserUseCase;
import com.pdgigs.domain.port.input.UpdateUserUseCase;
import com.pdgigs.domain.port.output.JwtTokenProvider;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.ChangePasswordRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateNameRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("UserController - Update Success Tests")
class UserControllerUpdateSuccessIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private User createUser(String id, String email, String name) {
        return new User(id, email, name, "encoded-pass", User.ROLE_USER, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should update name successfully and return new token")
    void updateName_ValidRequest_Returns200WithNewToken() {
        User existing = createUser("user-123", "user@example.com", "Old Name");
        User updated = createUser("user-123", "user@example.com", "New Name");

        when(getUserUseCase.getUserByEmail("user@example.com")).thenReturn(Mono.just(existing));
        when(updateUserUseCase.updateName("user-123", "New Name")).thenReturn(Mono.just(updated));
        when(jwtTokenProvider.generateToken(any())).thenReturn(Mono.just("new-token-123"));

        UpdateNameRequest request = new UpdateNameRequest("New Name");

        webTestClient.put()
                .uri("/users/me/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo("new-token-123")
                .jsonPath("$.tokenType").isEqualTo("Bearer")
                .jsonPath("$.user.name").isEqualTo("New Name");
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should change password successfully and return new token")
    void changePassword_ValidRequest_Returns200WithNewToken() {
        User existing = createUser("user-123", "user@example.com", "Test User");
        User updated = createUser("user-123", "user@example.com", "Test User");

        when(getUserUseCase.getUserByEmail("user@example.com")).thenReturn(Mono.just(existing));
        when(updateUserUseCase.changePassword("user-123", "oldPass", "newPass123"))
                .thenReturn(Mono.just(updated));
        when(jwtTokenProvider.generateToken(any())).thenReturn(Mono.just("token-after-pass-change"));

        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass123");

        webTestClient.put()
                .uri("/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo("token-after-pass-change")
                .jsonPath("$.tokenType").isEqualTo("Bearer");
    }
}