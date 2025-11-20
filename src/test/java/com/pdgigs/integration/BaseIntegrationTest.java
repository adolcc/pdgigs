package com.pdgigs.integration;

import com.pdgigs.infrastructure.adapter.input.rest.dto.request.RegisterRequest;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    protected static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)))
            .withStartupTimeout(Duration.ofSeconds(60));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    protected WebTestClient webTestClient;

    protected String jwtToken;
    protected String userEmail;

    @BeforeEach
    void setUpAuth() {
        userEmail = "test-" + System.currentTimeMillis() + "@example.com";
        try {
            jwtToken = registerAndGetToken(userEmail, "Test User", "password123");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to register test user in setUpAuth: " + e.getMessage(), e);
        }
    }

    protected String registerAndGetToken(String email, String name, String password) {
        RegisterRequest request = new RegisterRequest(email, name, password);

        // Usamos exchange() + expectBody(...) para obtener la respuesta de forma síncrona en test
        AuthResponse response = webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        if (response == null) {
            throw new IllegalStateException("Register returned null AuthResponse for email=" + email);
        }

        return response.token();
    }

    protected WebTestClient.RequestHeadersSpec<?> authenticatedRequest() {
        return webTestClient.get()
                .header("Authorization", "Bearer " + jwtToken);
    }
}