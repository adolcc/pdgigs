package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.exception.ConflictException;
import com.pdgigs.domain.exception.ValidationException;
import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.input.GetUserUseCase;
import com.pdgigs.domain.port.input.UpdateUserUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.ChangePasswordRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateEmailRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("UserController - Update Error Tests")
class UserControllerUpdateErrorIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    private User createUser(String id, String email, String name) {
        return new User(id, email, name, "encoded-pass", User.ROLE_USER, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should return 409 when email already exists")
    void updateEmail_EmailExists_Returns409() {
        User existing = createUser("user-123", "user@example.com", "Test User");

        when(getUserUseCase.getUserByEmail("user@example.com")).thenReturn(Mono.just(existing));
        when(updateUserUseCase.updateEmail(anyString(), anyString()))
                .thenReturn(Mono.error(ConflictException.userAlreadyExists("other@example.com")));

        UpdateEmailRequest request = new UpdateEmailRequest("other@example.com");

        webTestClient.put()
                .uri("/users/me/email")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should return 400 when current password is incorrect")
    void changePassword_WrongCurrentPassword_Returns400() {
        User existing = createUser("user-123", "user@example.com", "Test User");

        when(getUserUseCase.getUserByEmail("user@example.com")).thenReturn(Mono.just(existing));
        when(updateUserUseCase.changePassword(anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new ValidationException("currentPassword", "Current password is incorrect")));

        ChangePasswordRequest request = new ChangePasswordRequest("bad", "newPass123");

        webTestClient.put()
                .uri("/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
}