package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.input.GetUserUseCase;
import com.pdgigs.domain.port.input.UpdateUserUseCase;
import com.pdgigs.domain.port.output.JwtTokenProvider;
import com.pdgigs.domain.exception.ConflictException;
import com.pdgigs.domain.exception.validation.ValidationException;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.ChangePasswordRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateEmailRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateNameRequest;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.UserRestMapper;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.DomainExceptionHandler;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.GlobalFallbackHandler;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.ValidationExceptionHandler;
import com.pdgigs.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import({
        UserRestMapper.class,
        SecurityConfig.class,
        DomainExceptionHandler.class,
        ValidationExceptionHandler.class,
        GlobalFallbackHandler.class
})
@DisplayName("UserController - Update and Token Reissue Tests")
class UserControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private final String principalEmail = "pepe@example.com";

    private User makeUser(String id, String email, String name) {
        return new User(id, email, name, "encoded-pass", User.ROLE_USER, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = principalEmail, roles = "USER")
    @DisplayName("Update name returns new token and updated user")
    void updateName_ReissuesToken_Returns200() {
        User existing = makeUser("id-123", principalEmail, "pepe");
        User updated = makeUser("id-123", principalEmail, "nuevo");

        when(getUserUseCase.getUserByEmail(principalEmail)).thenReturn(Mono.just(existing));
        when(updateUserUseCase.updateName(anyString(), anyString())).thenReturn(Mono.just(updated));
        when(jwtTokenProvider.generateToken(any())).thenReturn(Mono.just("new-token-123"));

        UpdateNameRequest req = new UpdateNameRequest("nuevo");

        webTestClient.put()
                .uri("/users/me/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo("new-token-123")
                .jsonPath("$.tokenType").isEqualTo("Bearer")
                .jsonPath("$.user.id").isEqualTo("id-123")
                .jsonPath("$.user.email").isEqualTo(principalEmail)
                .jsonPath("$.user.name").isEqualTo("nuevo");
    }

    @Test
    @WithMockUser(username = principalEmail, roles = "USER")
    @DisplayName("Update email conflict -> 409")
    void updateEmail_Conflict_Returns409() {
        User existing = makeUser("id-123", principalEmail, "pepe");

        when(getUserUseCase.getUserByEmail(principalEmail)).thenReturn(Mono.just(existing));
        when(updateUserUseCase.updateEmail(anyString(), anyString()))
                .thenReturn(Mono.error(ConflictException.userAlreadyExists("other@example.com")));

        UpdateEmailRequest req = new UpdateEmailRequest("other@example.com");

        webTestClient.put()
                .uri("/users/me/email")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @WithMockUser(username = principalEmail, roles = "USER")
    @DisplayName("Change password success -> returns new token and user")
    void changePassword_Success_ReissuesToken() {
        User existing = makeUser("id-123", principalEmail, "pepe");
        User updated = makeUser("id-123", principalEmail, "pepe");

        when(getUserUseCase.getUserByEmail(principalEmail)).thenReturn(Mono.just(existing));
        when(updateUserUseCase.changePassword(anyString(), anyString(), anyString())).thenReturn(Mono.just(updated));
        when(jwtTokenProvider.generateToken(any())).thenReturn(Mono.just("token-after-pass-change"));

        ChangePasswordRequest req = new ChangePasswordRequest("oldPass", "newPass123");

        webTestClient.put()
                .uri("/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo("token-after-pass-change")
                .jsonPath("$.user.id").isEqualTo("id-123");
    }

    @Test
    @WithMockUser(username = principalEmail, roles = "USER")
    @DisplayName("Change password with wrong current -> 400 Bad Request")
    void changePassword_WrongCurrent_Returns400() {
        User existing = makeUser("id-123", principalEmail, "pepe");

        when(getUserUseCase.getUserByEmail(principalEmail)).thenReturn(Mono.just(existing));
        when(updateUserUseCase.changePassword(anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new ValidationException("currentPassword", "Current password is incorrect")));

        ChangePasswordRequest req = new ChangePasswordRequest("bad", "newPass123");

        webTestClient.put()
                .uri("/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest();
    }
}