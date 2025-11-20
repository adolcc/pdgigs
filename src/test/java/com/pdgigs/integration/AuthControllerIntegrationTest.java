package com.pdgigs.integration;

import com.pdgigs.infrastructure.adapter.input.rest.dto.request.LoginRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.RegisterRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.AuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Should register user successfully and return JWT token")
    void shouldRegisterUserSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "newuser@example.com",
                "New User",
                "password123"
        );

        // When & Then
        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    assertThat(response.token()).isNotNull();
                    assertThat(response.email()).isEqualTo("newuser@example.com");
                    assertThat(response.name()).isEqualTo("New User");
                    assertThat(response.role()).isEqualTo("ROLE_USER");
                });
    }

    @Test
    @DisplayName("Should fail to register with duplicate email")
    void shouldFailToRegisterWithDuplicateEmail() {
        // Given - First registration
        RegisterRequest request = new RegisterRequest(
                "duplicate@example.com",
                "User One",
                "password123"
        );

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        // When & Then - Second registration with same email
        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        // Given - Register first
        String email = "login-test@example.com";
        String password = "password123";
        registerAndGetToken(email, "Login Test", password);

        LoginRequest loginRequest = new LoginRequest(email, password);

        // When & Then
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    assertThat(response.token()).isNotNull();
                    assertThat(response.email()).isEqualTo(email);
                });
    }

    @Test
    @DisplayName("Should fail to login with invalid password")
    void shouldFailToLoginWithInvalidPassword() {
        // Given
        String email = "test-invalid@example.com";
        registerAndGetToken(email, "Test User", "correctPassword");

        LoginRequest loginRequest = new LoginRequest(email, "wrongPassword");

        // When & Then
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("Should fail to login with non-existent user")
    void shouldFailToLoginWithNonExistentUser() {
        // Given
        LoginRequest loginRequest = new LoginRequest(
                "nonexistent@example.com",
                "password123"
        );

        // When & Then
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("Should fail to register with invalid email format")
    void shouldFailToRegisterWithInvalidEmail() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "invalid-email",
                "Test User",
                "password123"
        );

        // When & Then
        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should fail to register with short password")
    void shouldFailToRegisterWithShortPassword() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "Test User",
                "pass"
        );

        // When & Then
        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
}